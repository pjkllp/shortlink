package com.nageoffer.shortlink.project.toolkit;

import jakarta.servlet.http.HttpServletRequest;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.IOException;

/**
 * User-Agent解析工具类，提取操作系统、浏览器等信息
 */
public class UserAgentParserUtil {
    // 初始化UA解析器（全局单例，避免重复创建）
    private static final Parser PARSER;
    static {
        PARSER = new Parser();
    }

    /**
     * 解析请求，获取操作系统名称（简化版）
     * @param request HTTP请求对象
     * @return 操作系统（如：Windows 10、Android 14、iOS 17、Mac OS X、Linux等）
     */
    public static String getOsFromRequest(HttpServletRequest request) {
        // 1. 获取请求头中的User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知系统";
        }

        // 2. 解析UA字符串
        Client client = PARSER.parse(userAgent);
        // 3. 提取操作系统名称 + 版本（也可只取名称）
        String osFamily = client.os.family; // 系统大类（如Windows、Android、iOS、Mac OS X）
        String osMajor = client.os.major;   // 主版本（如10、14、17）
        String osMinor = client.os.minor;   // 次版本（可选）

        // 4. 拼接简化的系统名称（根据需求调整）
        StringBuilder os = new StringBuilder(osFamily);
        if (osMajor != null && !osMajor.isEmpty()) {
            os.append(" ").append(osMajor);
            if (osMinor != null && !osMinor.isEmpty()) {
                os.append(".").append(osMinor);
            }
        }

        // 兼容处理：统一部分系统名称（可选）
        String osStr = os.toString();
        if (osStr.contains("Mac OS X")) {
            osStr = osStr.replace("Mac OS X", "macOS");
        } else if (osStr.contains("Windows NT")) {
            osStr = osStr.replace("Windows NT", "Windows");
        }

        return osStr;
    }

    // 若使用UserAgentUtils，简化版方法：
    /*
    public static String getOsFromRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "未知系统";
        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        OperatingSystem os = ua.getOperatingSystem();
        return os.getName(); // 直接返回系统名称，如Windows 10、Android
    }
    */
}