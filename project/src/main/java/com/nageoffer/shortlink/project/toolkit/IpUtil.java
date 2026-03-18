package com.nageoffer.shortlink.project.toolkit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

/**
 * IP地址获取工具类（适配代理/非代理场景）
 */
public class IpUtil {

    // 排除的内网/代理IP前缀
    private static final List<String> EXCLUDE_IPS = Arrays.asList("127.", "192.168.", "10.", "172.", "0:0:0:0:0:0:0:1");

    /**
     * 获取用户真实IP地址
     */
    public static String getRealIp(HttpServletRequest request) {
        // 1. 先从代理请求头中获取（优先级从高到低）
        String ip = request.getHeader("X-Forwarded-For");
        if (isInvalidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        // 2. 所有代理头都没有，取原生IP
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
            // 处理本地回环地址（IPv6）
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ip = "127.0.0.1";
            }
        }

        // 3. 处理 X-Forwarded-For 多个IP的情况（格式：用户IP, 代理IP1, 代理IP2）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 4. 过滤内网IP（可选，根据业务需求）
        if (isInnerIp(ip)) {
            return "127.0.0.1"; // 内网IP统一返回本地地址
        }

        return ip;
    }

    /**
     * 判断IP是否无效（空/未知）
     */
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip);
    }

    /**
     * 判断是否为内网IP
     */
    private static boolean isInnerIp(String ip) {
        if (ip == null) {
            return false;
        }
        for (String prefix : EXCLUDE_IPS) {
            if (ip.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}