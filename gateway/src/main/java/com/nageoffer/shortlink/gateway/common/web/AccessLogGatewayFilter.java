package com.nageoffer.shortlink.gateway.common.web;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.nageoffer.shortlink.gateway.common.entity.AccessLogDO;
import com.nageoffer.shortlink.gateway.common.entity.AccessLogStream;
import com.nageoffer.shortlink.gateway.toolkit.IpUtil;
import com.nageoffer.shortlink.gateway.toolkit.UserAgentParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nageoffer.shortlink.gateway.common.constant.RedisConstant.ACCESS_LOG_STREAM;

@Component
@Slf4j
public class AccessLogGatewayFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_PATH = "/api/short-link/admin/v1/login";

    private final StringRedisTemplate stringRedisTemplate;

    public AccessLogGatewayFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 仅记录：1）登录接口 POST；2）短链直跳（单段路径 GET/HEAD，且非 /api 等保留段）
     */
    private static boolean shouldRecordAccessLog(String path, String method) {
        if (path == null || method == null) {
            return false;
        }
        if (LOGIN_PATH.equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            return false;
        }
        if (!path.startsWith("/") || "/".equals(path)) {
            return false;
        }
        if (path.indexOf('/', 1) >= 0) {
            return false;
        }
        String segment = path.substring(1);
        if (segment.isEmpty()) {
            return false;
        }
        if ("api".equalsIgnoreCase(segment) || "favicon.ico".equalsIgnoreCase(segment)) {
            return false;
        }
        return true;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String path = serverHttpRequest.getPath().value();
        String method = serverHttpRequest.getMethod().name();
        if (shouldRecordAccessLog(path, method)) {
            String userAgent = serverHttpRequest.getHeaders().getFirst("User-Agent");
            String browser = UserAgentParserUtil.getBrowser(userAgent);
            String device = UserAgentParserUtil.getDevice(userAgent);
            String network = UserAgentParserUtil.getNetworkType(userAgent);
            String os = UserAgentParserUtil.getOs(userAgent);
            String ip = IpUtil.getRealIp(serverHttpRequest);

            AccessLogStream accessLogStream = AccessLogStream.builder()
                    .accessTime(new Date())
                    .ip(ip)
                    .path(path)
                    .browser(browser)
                    .method(method)
                    .network(network)
                    .os(os)
                    .device(device)
                    .eventId(IdUtil.getSnowflakeNextIdStr())
                    .build();
            recordAccessLogAsync(accessLogStream);
        }
        return chain.filter(exchange);
    }

    private final RedisScript<Void> redisScript=RedisScript.of(
            new ClassPathResource("/lua/zaddSream.lua")
    );

    private final String QPS_ZSET_KEY="qps_zset_key";
    private void recordAccessLogAsync(AccessLogStream accessLogStream) {
        Mono.fromRunnable(() -> {
            String json = null;
            try {
                json = JSON.toJSONString(accessLogStream);
                stringRedisTemplate.opsForStream().add(
                        StreamRecords.string(
                                Map.of("payload", json)
                        ).withStreamKey(ACCESS_LOG_STREAM)
                );
                //计算流qps，动态清理流
                stringRedisTemplate.execute(redisScript, List.of(QPS_ZSET_KEY));
                log.info("监控到外来ip:{}",accessLogStream.getIp());
            } catch (Exception e) {
                log.error("记录访问日志失败，原因:{},需要记录的消息:{}", e, json);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
