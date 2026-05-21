package com.nageoffer.shortlink.project.common.web;

import cn.hutool.core.util.StrUtil;
import com.nageoffer.shortlink.project.common.biz.user.UserContext;
import com.nageoffer.shortlink.project.common.biz.user.UserInfo;
import com.nageoffer.shortlink.project.common.exceptions.ClientException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SetInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("请求路径: " + request.getRequestURI());
        String username = request.getHeader("username");
        String isAdminHeader = request.getHeader("isAdmin");
        if (StrUtil.isBlank(username) || StrUtil.isBlank(isAdminHeader)) {
            throw new ClientException("用户未登录");
        }
        Integer isAdmin = 0;
        if ("1".equals(isAdminHeader)) {
            isAdmin = 1;
        }
        UserContext.setUser(UserInfo.builder().username(username).isAdmin(isAdmin).build());
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserContext.removeUser();
    }
}
