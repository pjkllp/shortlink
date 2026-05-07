package com.nageoffer.shortlink.admin.remote.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Relay gateway-injected auth headers to downstream Feign calls.
 */
@Configuration
public class FeignHeaderRelayConfig {

    @Bean
    public RequestInterceptor userHeaderRelayInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }
            HttpServletRequest request = attrs.getRequest();
            if (request == null) {
                return;
            }
            String username = request.getHeader("username");
            String isAdmin = request.getHeader("isAdmin");
            if (username != null && !username.isBlank()) {
                template.header("username", username);
            }
            if (isAdmin != null && !isAdmin.isBlank()) {
                template.header("isAdmin", isAdmin);
            }
        };
    }
}

