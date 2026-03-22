package com.nageoffer.shortlink.gateway.common.web;

import com.nageoffer.shortlink.gateway.common.biz.user.UserContext;
import com.nageoffer.shortlink.gateway.common.biz.user.UserInfo;
import com.nageoffer.shortlink.gateway.common.constant.Constant;
import com.nageoffer.shortlink.gateway.common.exceptions.ClientException;
import com.nageoffer.shortlink.gateway.toolkit.JwtUtil;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.List;

/**
 * 登录鉴权全局过滤器（替代原 Spring MVC 拦截器）
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements WebFilter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final Pattern WHITE_LIST_PATTERN = Pattern.compile(
            "^/api/short-link/admin/v1/user/[^/]+$" +
                    "|^/api/short-link/admin/v1/has_username/?$" +
                    "|^/api/short-link/admin/v1/user$" +
                    "|^/api/short-link/admin/v1/login$" +
                    "|^/api/short-link/admin/v1/logout$"
    );

    private static String USER_RISK_CONTROL_LUA_PATH="jetbrains://idea/navigate/reference?project=shortlink&path=lua/user_rist_contro.lua";

    /**
     * 核心过滤逻辑（对应原 preHandle 方法）
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getPath().toString();

        // ========== 关键：白名单放行 /api/short-link/admin/v1/user/** ==========
        if (WHITE_LIST_PATTERN.matcher(path).matches()) {
            return chain.filter(exchange); // 直接放行，不做鉴权
        }
        // 1. 从 Header 获取 Token，处理前缀
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return writeUnauthorizedResponse(response, "用户未登录");
        }

        token = token.substring(7).trim(); // 去除 "Bearer " 前缀和空格

        // 2. 解析 JWT 获取用户名
        String username = JwtUtil.parseJwt(token);

        // 3. 从 Redis 校验 Token 有效性
        String redisToken = stringRedisTemplate.opsForValue().get(Constant.USER_LOGIN + username);
        if (username == null || redisToken == null || redisToken.isEmpty() || !redisToken.equals(token)) {
            return writeUnauthorizedResponse(response, "用户未登录");
        }

        DefaultRedisScript<Long> script=new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_RISK_CONTROL_LUA_PATH)));
        script.setResultType(Long.class);
        Long result = stringRedisTemplate.execute(script, List.of(
                "rate:limit:user:"+username+"path:"+path
        ), 3, 1);

        if(Optional.ofNullable(result).orElse(0L) ==0){
            throw new ClientException("请求过多请稍后再试");
        }

        ServerHttpRequest newRequest = request.mutate().header("username", username).build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);
    }

    /**
     * 写入 401 未登录响应（替代原 response.getWriter() 写法）
     */
    private Mono<Void> writeUnauthorizedResponse(ServerHttpResponse response, String msg) {
        // 设置响应状态码和格式
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构造和原拦截器一致的 JSON 响应
        String json = String.format("{\"code\":\"0\",\"msg\":\"%s\",\"data\":\"null\"}", msg);
        DataBuffer buffer = response.bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));

        // 返回响应，中断请求
        return response.writeWith(Mono.just(buffer));
    }
}