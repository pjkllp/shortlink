package com.nageoffer.shortlink.admin.common.Intercepors;

import com.nageoffer.shortlink.admin.common.constant.OtherConstant;
import com.nageoffer.shortlink.admin.toolkit.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    // 在请求处理之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim(); // 去除前缀和可能的空格
        } else {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"0\",\"msg\":\"用户未登录\",\"data\":\"null\"}");
            response.getWriter().flush();
            response.getWriter().close();
            // 无有效令牌，直接返回未登录
            return false;
        }

        String username = JwtUtil.parseJwt(token);
        String token2 = stringRedisTemplate.opsForValue().get(OtherConstant.USER_LOGIN + username);
        if(username!=null&&token2!=null&&!token2.isEmpty()&&token2.equals(token)){
            OtherConstant.USER_MESSAGE.set(JwtUtil.parseJwt(token));
            return true;
        }else{
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"0\",\"msg\":\"用户未登录\",\"data\":\"null\"}");
            response.getWriter().flush();
            response.getWriter().close();
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        OtherConstant.USER_MESSAGE.remove();
    }
}
