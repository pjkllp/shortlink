package com.nageoffer.shortlink.project.config;

import com.nageoffer.shortlink.project.common.web.AdminAccessInterceptor;
import com.nageoffer.shortlink.project.common.web.SetInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final SetInterceptor setInterceptor;
    private final AdminAccessInterceptor adminAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(setInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(adminAccessInterceptor)
                .addPathPatterns("/api/short-link/project/v1/monitor/**");
    }
}
