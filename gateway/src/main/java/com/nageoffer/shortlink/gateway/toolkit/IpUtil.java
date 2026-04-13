package com.nageoffer.shortlink.gateway.toolkit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class IpUtil {

    // 常见的代理 IP 请求头，按优先级排序
    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "X-Real-IP"
    );

    /**
     * 从 ServerHttpRequest 中获取真实客户端 IP
     */
    public static String getRealIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // 1. 遍历所有可能的代理头
        for (String header : IP_HEADERS) {
            String ip = headers.getFirst(header);
            if (isValidIp(ip)) {
                // 对于 X-Forwarded-For，格式通常是: client_ip, proxy1_ip, proxy2_ip
                // 我们只需要第一个非 unknown 的 IP
                if (ip.contains(",")) {
                    return Arrays.stream(ip.split(","))
                            .map(String::trim)
                            .filter(IpUtil::isValidIp)
                            .findFirst()
                            .orElse(null);
                }
                return ip;
            }
        }

        // 2. 如果没有代理头，直接获取 RemoteAddress
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    /**
     * 校验 IP 是否有效（非空且不是 "unknown"）
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip.trim());
    }
}