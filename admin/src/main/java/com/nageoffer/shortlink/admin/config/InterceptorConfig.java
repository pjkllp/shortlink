package com.nageoffer.shortlink.admin.config;

import com.nageoffer.shortlink.admin.common.Intercepors.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    // 方式1：直接@Autowired注入（更稳妥，避免构造器注入的加载顺序问题）
    private final LoginInterceptor loginInterceptor;

    // 构造器注入需要确保LoginInterceptor是Spring Bean（加了@Component）
    public InterceptorConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                // 修复点1：统一路径格式（去掉末尾多余/，用*匹配路径变量）
                // 修复点2：增加精确匹配+通配符匹配，覆盖所有场景
                .excludePathPatterns(
                        // 精确匹配login接口（重点：去掉末尾/，和实际请求路径一致）
                        "/api/short-link/v1/login",
                        // 兼容可能的末尾/场景
                        "/api/short-link/v1/login/",
                        // 检查用户名接口（去掉末尾/，避免匹配不一致）
                        "/api/short-link/v1/has_username",
                        // 用户相关接口（用*匹配路径变量，而非{username}）
                        "/api/short-link/v1/user/*",
                        // 注册用户接口
                        "/api/short-link/v1/user",
                        "/api/short-link/v1/logout"
                );
    }
}