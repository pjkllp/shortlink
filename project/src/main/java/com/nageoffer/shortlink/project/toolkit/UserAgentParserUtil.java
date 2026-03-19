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

    // ========== 新增解析浏览器方法 ==========
    /**
     * 解析请求，获取浏览器名称+版本（如：Chrome 122、Safari 17、微信浏览器 8.0）
     * @param request HTTP请求对象
     * @return 浏览器信息
     */
    public static String getBrowserFromRequest(HttpServletRequest request) {
        // 1. 获取User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知浏览器";
        }

        // 2. 解析UA获取浏览器信息
        Client client = PARSER.parse(userAgent);
        String browserFamily = client.userAgent.family; // 浏览器大类（Chrome、Safari、Firefox等）
        String browserMajor = client.userAgent.major;   // 主版本（122、17、115等）
        String browserMinor = client.userAgent.minor;   // 次版本（可选）

        // 3. 拼接浏览器名称+版本
        StringBuilder browser = new StringBuilder(browserFamily);
        if (browserMajor != null && !browserMajor.isEmpty()) {
            browser.append(" ").append(browserMajor);
            if (browserMinor != null && !browserMinor.isEmpty()) {
                browser.append(".").append(browserMinor);
            }
        }

        // 4. 兼容处理：识别微信/支付宝内置浏览器（关键补充）
        String browserStr = browser.toString();
        if (userAgent.contains("MicroMessenger")) {
            // 提取微信版本（如 MicroMessenger/8.0.40 → 微信浏览器 8.0.40）
            String wechatVersion = userAgent.split("MicroMessenger/")[1].split(" ")[0];
            browserStr = "微信浏览器 " + wechatVersion;
        } else if (userAgent.contains("AlipayClient")) {
            browserStr = "支付宝内置浏览器";
        } else if (userAgent.contains("QQBrowser")) {
            browserStr = browserStr.replace("QQBrowser", "QQ浏览器");
        }

        return browserStr;
    }
    // ========== 新增方法：解析设备类型 ==========
    /**
     * 解析请求，获取设备类型（手机/电脑/平板/小程序/爬虫等）
     * @param request HTTP请求对象
     * @return 设备类型
     */
    public static String getDeviceFromRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知设备";
        }

        // 1. 优先识别特殊设备/场景
        if (userAgent.contains("MicroMessenger") && userAgent.contains("miniProgram")) {
            return "微信小程序"; // 微信小程序
        } else if (userAgent.contains("AlipayClient") && userAgent.contains("miniProgram")) {
            return "支付宝小程序"; // 支付宝小程序
        } else if (userAgent.contains("Spider") || userAgent.contains("bot") || userAgent.contains("crawler")) {
            return "爬虫设备"; // 爬虫/机器人
        }

        // 2. 解析通用设备类型
        Client client = PARSER.parse(userAgent);
        String deviceFamily = client.device.family; // 设备大类

        // 3. 统一设备类型名称（适配不同UA解析结果）
        return switch (deviceFamily.toLowerCase()) {
            case "iphone", "android", "huawei", "xiaomi", "oppo", "vivo", "samsung" -> "手机";
            case "ipad", "android tablet", "tablet" -> "平板";
            case "pc", "windows", "mac", "linux" -> "电脑";
            case "unknown" -> {
                // 兜底判断：通过系统类型反推设备
                if (userAgent.contains("Mobile")) yield "手机";
                else if (userAgent.contains("Tablet")) yield "平板";
                else yield "未知设备";
            }
            default -> deviceFamily;
        };
    }

    // ========== 新增方法：解析网络访问类型 ==========
    /**
     * 解析请求，获取网络访问类型（WiFi/4G/5G/蜂窝网络/未知）
     * @param request HTTP请求对象
     * @return 网络类型
     */
    public static String getNetworkTypeFromRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // 1. 优先获取移动端浏览器/小程序的网络类型扩展字段
        String networkType = request.getHeader("X-Network-Type"); // 部分移动端浏览器会携带
        if (networkType != null && !networkType.isEmpty()) {
            return formatNetworkType(networkType);
        }

        // 2. 解析UA中的网络特征（适配微信/手机浏览器）
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知网络";
        }

        // 微信内置浏览器的UA可能包含网络特征
        if (userAgent.contains("WIFI")) {
            return "WiFi";
        } else if (userAgent.contains("4G") || userAgent.contains("LTE")) {
            return "4G";
        } else if (userAgent.contains("5G") || userAgent.contains("NR")) {
            return "5G";
        } else if (userAgent.contains("3G") || userAgent.contains("WCDMA") || userAgent.contains("TD-SCDMA")) {
            return "3G";
        } else if (userAgent.contains("2G") || userAgent.contains("GSM")) {
            return "2G";
        } else if (userAgent.contains("Mobile") && !userAgent.contains("WIFI")) {
            return "蜂窝网络"; // 无法精准识别的移动网络
        } else {
            return "未知网络"; // PC端/无特征的网络
        }
    }
    /**
     * 格式化网络类型名称（统一不同客户端的字段值）
     * @param rawType 原始网络类型字符串
     * @return 标准化名称
     */
    private static String formatNetworkType(String rawType) {
        return switch (rawType.toLowerCase()) {
            case "wifi", "wlan" -> "WiFi";
            case "4g", "lte" -> "4G";
            case "5g", "nr" -> "5G";
            case "3g" -> "3G";
            case "2g" -> "2G";
            case "mobile", "cellular" -> "蜂窝网络";
            default -> "未知网络";
        };
    }
}