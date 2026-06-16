package com.nageoffer.shortlink.project.Comsumer;

import cn.hutool.Hutool;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.common.exceptions.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.AccessLogStream;
import com.nageoffer.shortlink.project.dto.ShortLinkStatsMessageDTO;
import com.nageoffer.shortlink.project.toolkit.AmapIpUtil;
import com.nageoffer.shortlink.project.toolkit.DO.AmapIpLocationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 短链接监控数据消费者（异步处理入库）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class doMessage {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AmapIpUtil amapIpUtil;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final AccessLogMapper accessLogMapper;

    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String messageStr) {
        String idempotentKey = null;
        String eventId = null;
        try {
            String jsonStr;
            String cleanMsg = messageStr.replaceAll("^\"|\"$", "");
            // 先尝试 Base64 解码（兼容编码的消息）
            try {
                byte[] decodeBytes = Base64.getDecoder().decode(cleanMsg);
                jsonStr = new String(decodeBytes);
            } catch (IllegalArgumentException e) {
                // 如果不是 Base64 编码，直接用原始字符串
                jsonStr = messageStr;
            }
            ShortLinkStatsMessageDTO msg = JSON.parseObject(jsonStr, ShortLinkStatsMessageDTO.class);
            eventId = msg.getEventId();
            if (StringUtils.isNotBlank(eventId)) {
                idempotentKey = "mq:shortlink:stats:dedup:" + eventId;
            }
            if (StringUtils.isNotBlank(idempotentKey)
                    && Boolean.FALSE.equals(stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "-", 30, TimeUnit.MINUTES))) {
                log.warn("{}消息重复消费拦截",eventId);
                return;
            }
            String gid = msg.getGid();
            if (gid == null) {
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, msg.getFullShortUrl()));
                if (shortLinkGotoDO == null) {
                    throw new ServiceException("服务器数据异常");
                }
                gid = shortLinkGotoDO.getGid();
            }
            String ip = msg.getIp();
            Long added = stringRedisTemplate.opsForSet().add("short_link_uip_stats", ip);
            boolean isNewUip = added != null && added > 0;

            // 1. 处理PV/UV/UIP统计
            LinkAccessStatsDO accessStats = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(msg.isNewUv() ? 1 : 0)
                    .uip(isNewUip ? 1 : 0)
                    .gid(gid)
                    .date(msg.getAccessTime())
                    .hour(msg.getHour())
                    .weekday(msg.getWeekday())
                    .fullShortUrl(msg.getFullShortUrl())
                    .build();
            linkAccessStatsMapper.shortLinkStats(accessStats);

            // 4. 解析地域信息（高德IP定位）
            String city = "未知";
            String province = "未知";
            String adcode = "未知";
            try {
                AmapIpLocationResult locationByIp = amapIpUtil.getLocationByIp(ip);
                if (locationByIp != null) {
                    city = StrUtil.isBlank(locationByIp.getCity()) || Objects.equals(locationByIp.getCity(), "[]") ? "未知" : locationByIp.getCity();
                    province = StrUtil.isBlank(locationByIp.getProvince()) || Objects.equals(locationByIp.getProvince(), "[]") ? "未知" : locationByIp.getProvince();
                    adcode = StrUtil.isBlank(locationByIp.getAdcode()) || Objects.equals(locationByIp.getAdcode(), "[]") ? "未知" : locationByIp.getAdcode();
                }
            } catch (Exception e) {
                log.warn("高德地图定位失败:{}", e.getMessage());
            }

            // 2. 处理地域统计
            LinkLocaleStatsDO localeStats = LinkLocaleStatsDO.builder()
                    .cnt(1)
                    .fullShortUrl(msg.getFullShortUrl())
                    .city(city)
                    .province(province)
                    .ascode(adcode)
                    .gid(gid)
                    .country("中国")
                    .date(msg.getAccessTime())
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleStats(localeStats);

            // 3. 处理OS统计
            LinkOsStatsDO osStats = LinkOsStatsDO.builder()
                    .os(msg.getOs())
                    .cnt(1)
                    .date(msg.getAccessTime())
                    .fullShortUrl(msg.getFullShortUrl())
                    .gid(gid)
                    .build();
            linkOsStatsMapper.shortLinkOsStats(osStats);

            // 4. 处理浏览器统计
            LinkBrowserStatsDO browserStats = LinkBrowserStatsDO.builder()
                    .fullShortUrl(msg.getFullShortUrl())
                    .browser(msg.getBrowser())
                    .cnt(1)
                    .gid(gid)
                    .date(msg.getAccessTime())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserStats(browserStats);

            // 5. 处理设备统计
            LinkDeviceStatsDO deviceStats = LinkDeviceStatsDO.builder()
                    .gid(gid)
                    .cnt(1)
                    .date(msg.getAccessTime())
                    .device(msg.getDevice())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceStats(deviceStats);

            // 6. 处理网络统计
            LinkNetworkStatsDO networkStats = LinkNetworkStatsDO.builder()
                    .fullShortUrl(msg.getFullShortUrl())
                    .gid(gid)
                    .date(msg.getAccessTime())
                    .network(msg.getNetwork())
                    .cnt(1)
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkStats(networkStats);

            // 7. 处理访问日志
            LinkAccessLogsDO accessLogs = LinkAccessLogsDO.builder()
                    .ip(msg.getIp())
                    .locale(city)
                    .network(msg.getNetwork())
                    .fullShortUrl(msg.getFullShortUrl())
                    .browser(msg.getBrowser())
                    .user(msg.getUv())
                    .os(msg.getOs())
                    .device(msg.getDevice())
                    .build();
            int insert = linkAccessLogsMapper.insert(accessLogs);
            log.info("访问日志入库结果:{}", insert > 0 ? "成功" : "失败");
            LinkAccessLogsDO checkRecord = linkAccessLogsMapper.selectById(accessLogs.getId());
            log.info("插入后即时查询记录：{}", checkRecord != null ? "存在" : "不存在");
            log.info("监控数据入库成功，fullShortUrl:{}", msg.getFullShortUrl());
        } catch (Exception e) {
            if (StringUtils.isNotBlank(idempotentKey)) {
                stringRedisTemplate.delete(idempotentKey);
            }
            log.error("数据入库失败，触发重试。message={}", messageStr, e);
            throw new RuntimeException("短链接统计消息消费失败，触发重试", e);
        }
    }

    public void recordAccessLog(String messageStr){
        String jsonStr;
        String cleanMsg = messageStr.replaceAll("^\"|\"$", "");
        try {
            byte[] decodeBytes = Base64.getDecoder().decode(cleanMsg);
            jsonStr = new String(decodeBytes);
        } catch (IllegalArgumentException e) {
            jsonStr = messageStr;
        }
        AccessLogStream accessLogStream = JSON.parseObject(jsonStr, AccessLogStream.class);
        AccessLogDO accessLogDO = BeanUtil.toBean(accessLogStream, AccessLogDO.class);
        String city=null;
        String province=null;
        try {
            AmapIpLocationResult locationByIp = amapIpUtil.getLocationByIp(accessLogDO.getIp());
            if (locationByIp != null) {
                city = StrUtil.isBlank(locationByIp.getCity()) || Objects.equals(locationByIp.getCity(), "[]") ? "未知" : locationByIp.getCity();
                province = StrUtil.isBlank(locationByIp.getProvince()) || Objects.equals(locationByIp.getProvince(), "[]") ? "未知" : locationByIp.getProvince();
            }
        } catch (Exception e) {
            log.warn("高德地图定位失败:{}", e.getMessage());
        }
        accessLogDO.setProvince(province);
        accessLogDO.setCity(city);
        accessLogMapper.insert(accessLogDO);
    }
}