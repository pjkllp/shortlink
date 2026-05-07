package com.nageoffer.shortlink.project.common.web;

import com.nageoffer.shortlink.project.common.biz.user.UserContext;
import com.nageoffer.shortlink.project.common.exceptions.ClientException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 仅管理员可访问的接口拦截（与网关透传的 isAdmin 头、{@link UserContext} 一致）
 */
@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!Integer.valueOf(1).equals(UserContext.getIsAdmin())) {
            throw new ClientException("无权限：仅管理员可访问监控相关接口");
        }
        return true;
    }
}
