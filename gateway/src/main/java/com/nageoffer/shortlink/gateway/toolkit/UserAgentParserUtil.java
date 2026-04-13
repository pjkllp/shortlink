package com.nageoffer.shortlink.gateway.toolkit;

import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;

public class UserAgentParserUtil {

    /**
     * 解析浏览器名称
     */
    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        UserAgent ua = UserAgentUtil.parse(userAgent);
        Browser browser = ua.getBrowser();
        return browser.getName(); // 输出如：Chrome 124, Firefox 125, Safari 17
    }

    /**
     * 解析设备类型
     * 返回：Mobile(手机), Tablet(平板), PC(电脑), Unknown
     */
    public static String getDevice(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        UserAgent ua = UserAgentUtil.parse(userAgent);
        if (ua.isMobile()) {
            return "Mobile";
        } else if (ua.getPlatform().isMobile()) {
            return "Tablet"; // 简单判定平板
        }
        return "PC";
    }

    /**
     * 解析操作系统
     */
    public static String getOs(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        UserAgent ua = UserAgentUtil.parse(userAgent);
        OS os = ua.getOs();
        return os.getName(); // 输出如：Windows 11, Mac OS X, Android 14, iOS 17
    }

    /**
     * 解析网络类型（注意：UA 中通常不直接包含网络类型，此为扩展逻辑）
     * 只能通过 UA 中的特殊标识粗略判断，如 "4G", "5G", "Wifi" 等
     * 更准确的网络类型建议从运营商侧或其他请求头获取
     */
    public static String getNetworkType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        String lowerUa = userAgent.toLowerCase();
        if (lowerUa.contains("5g")) return "5G";
        if (lowerUa.contains("4g")) return "4G";
        if (lowerUa.contains("3g")) return "3G";
        if (lowerUa.contains("wifi") || lowerUa.contains("wireless")) return "WiFi";
        return "Unknown";
    }
}