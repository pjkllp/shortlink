package com.nageoffer.shortlink.project.dto;

import com.nageoffer.shortlink.project.toolkit.UserAgentParserUtil;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 短链接访问监控消息体
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsMessageDTO {
    // 核心字段
    private String fullShortUrl;
    private String gid;

    private String ip;
    private String os;
    private String browser;
    private String device;
    private String network;
    private boolean isNewUv;
    private String uv;
    // 时间字段
    private Date accessTime;
    private int hour;
    private int weekday;
    //业务唯一id，用来防止幂等
    private String eventId;
}