package com.nageoffer.shortlink.gateway.common.web;

import com.nageoffer.shortlink.gateway.common.enums.UserErrorCode;
import com.nageoffer.shortlink.gateway.common.exceptions.AbstractException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器
 */
@Component
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 1. 处理自定义 ClientException
        if (ex instanceof AbstractException e) {
            String json = String.format(
                    "{\"code\":\"%s\",\"msg\":\"%s\",\"data\":null}",
                    e.errorCode,
                    e.errorMessage
            );
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 或对应业务码
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }

        // 2. 处理其他未知异常（默认 500）
        String json = "{\"code\":\"500\",\"msg\":\"系统异常\",\"data\":null}";
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}