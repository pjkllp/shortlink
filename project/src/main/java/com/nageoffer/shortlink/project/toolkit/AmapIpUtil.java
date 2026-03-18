package com.nageoffer.shortlink.project.toolkit;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nageoffer.shortlink.project.toolkit.DO.AmapIpLocationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AmapIpUtil {

    @Value("${gaode.amap.key}")
    private String AMAP_KEY;
    private static final String AMAP_IP_URL = "https://restapi.amap.com/v3/ip?ip=%s&key=%s&output=json";

    // Spring 内置 RestTemplate，零额外依赖
    private final RestTemplate restTemplate = new RestTemplate();
    // Spring 内置 Jackson，用于 JSON 解析
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据 IP 获取高德定位信息
     * @param ip 用户真实 IP（如 114.114.114.114）
     * @return AmapIpLocationResult 定位结果实体类，失败时返回 null 或部分字段为 null
     */
    public AmapIpLocationResult getLocationByIp(String ip) {
        try {
            // 1. 拼接完整请求 URL
            String url = String.format(AMAP_IP_URL, ip, AMAP_KEY);
            // 2. 调用高德 API，获取原始 JSON 响应
            String response = restTemplate.getForObject(url, String.class);
            // 3. 核心修改：用 FastJSON 解析（自动忽略未知字段）
            AmapIpLocationResult result = JSON.parseObject(response, AmapIpLocationResult.class);

            // 4. 新增：校验接口返回状态（处理 KEY 错误等问题）
            if (result != null && "0".equals(result.getStatus())) {
                System.err.println("高德IP定位失败：" + result.getInfo() + "，错误码：" + result.getInfocode());
                return null;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}