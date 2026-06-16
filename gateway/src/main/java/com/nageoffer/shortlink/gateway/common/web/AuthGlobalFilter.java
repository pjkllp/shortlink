package com.nageoffer.shortlink.gateway.common.web;

import com.nageoffer.shortlink.gateway.common.biz.user.UserContext;
import com.nageoffer.shortlink.gateway.common.biz.user.UserInfo;
import com.nageoffer.shortlink.gateway.common.constant.Constant;
import com.nageoffer.shortlink.gateway.common.exceptions.ClientException;
import com.nageoffer.shortlink.gateway.toolkit.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.List;

/**
 * 登录鉴权全局过滤器（网关标准GlobalFilter，替代原WebFilter）
 */
@Component
@RequiredArgsConstructor
@Order(0)
// 新增Ordered接口，网关标准规范
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate stringRedisTemplate;

    private static final Pattern WHITE_LIST_PATTERN = Pattern.compile(
            // 管理员用户相关接口
            "(^/api/short-link/admin/v1/user/[^/]+$)" +
                    "|(^/api/short-link/admin/v1/has_username/?$)" +
                    "|(^/api/short-link/admin/v1/getCode$)" +
                    "|(^/api/short-link/admin/v1/user$)" +
                    "|(^/api/short-link/admin/v1/login$)" +
                    "|(^/api/short-link/admin/v1/refreshLogin$)" +
                    "|(^/api/short-link/admin/v1/logout$)" +
                    // ✅ 短链接重定向接口
                    "|(^/[^/]+/?$)" +
                    // ✅ 根路径放行
                    "|(^/$)" +
                    // ✅ 页面路由白名单 /page/**
                    "|(^/page/.*$)"
    );

    private static String USER_RISK_CONTROL_LUA_PATH = "jetbrains://idea/navigate/reference?project=shortlink&path=lua/user_rist_contro.lua";

    /**
     * 网关标准过滤方法（唯一保留的filter，删除WebFilterChain版本）
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ========== 你原来全部鉴权逻辑完整迁移至此，一行不动 ==========
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getPath().toString();

        // 白名单放行
        if (WHITE_LIST_PATTERN.matcher(path).matches()) {
            return chain.filter(exchange);
        }
        // 1. 从 Header 获取 Token，处理Bearer前缀
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return writeUnauthorizedResponse(response, "用户未登录");
        }

        token = token.substring(7).trim();

        // 2. 解析JWT用户名
        String username = JwtUtil.parseJwt(token);

        // 3. Redis校验登录态
        String redisToken = stringRedisTemplate.opsForValue().get(Constant.USER_LOGIN + username);
        if (username == null || redisToken == null || redisToken.isEmpty() || !redisToken.equals(token)) {
            return writeUnauthorizedResponse(response, "用户未登录");
        }

        String isAdmin = stringRedisTemplate.opsForValue().get(Constant.USER_IS_ADMIN + username);
        if (isAdmin == null || isAdmin.isEmpty()) {
            isAdmin = "0";
        }

        // 透传用户信息到下游服务
        ServerHttpRequest newRequest = request.mutate()
                .header("username", username)
                .header("isAdmin", isAdmin)
                .build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);
    }

    /**
     * 401未登录统一返回，逻辑不变
     */
    private Mono<Void> writeUnauthorizedResponse(ServerHttpResponse response, String msg) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = String.format("{\"code\":\"0\",\"msg\":\"%s\",\"data\":\"null\"}", msg);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Ordered接口实现，和@Order(0)保持一致，网关标准写法
     */
    @Override
    public int getOrder() {
        return 0;
    }
}