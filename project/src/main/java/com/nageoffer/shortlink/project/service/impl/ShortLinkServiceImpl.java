package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.enums.ValidDataTypeEnum;
import com.nageoffer.shortlink.project.common.biz.user.UserContext;
import com.nageoffer.shortlink.project.common.exceptions.ClientException;
import com.nageoffer.shortlink.project.common.exceptions.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.ShortLinkStatsMessageDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.*;
import com.nageoffer.shortlink.project.toolkit.DO.AmapIpLocationResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.nageoffer.shortlink.project.Comsumer.Stream.StreamImpl.ShortLinkStatsStreamConsumer.STATS_STREAM_GROUP;
import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口实现层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private static final String STATS_STREAM_KEY = "short-link-stats-stream";
    private static final int USER_MAX_SHORT_LINK_COUNT = 10;

    private final RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

    private final ShortLinkMapper shortLinkMapper;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final LinkAccessStatsMapper shortLinkAccessStatsDOMapper;

    private final LinkLocaleStatsMapper linkLocalStatsMapper;

    private final AmapIpUtil amapIpUtil;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkGroupMapper linkGroupMapper;

    private final ObjectProvider<RocketMQTemplate> rocketMQTemplateProvider;

    @Value("${stats.mq.enabled:false}")
    private boolean statsMqEnabled;

    @Value("${short-link.domain.default:pengwater.xin}")
//    @Value("${short-link.domain.default:nurl.local}")
    private String createShortLinkDefaultDomain;
    @Value("${short-link.protocol:https}")
    private String shortLinkProtocol;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam,HttpServletRequest request,HttpServletResponse response) throws IOException {
        validateUserShortLinkQuota();
        String shortLinkSuffix = generateSuffix(requestParam);
        String originUrl = requestParam.getOriginUrl();
        String scheme=null;
        if (originUrl.startsWith("https://")){
            scheme="https";
        }else {
            throw new ClientException("此网站不安全");
        }
        String fullShortUrl = StrBuilder.create(scheme)
                .append("://")
                .append(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix).toString();
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(fullShortUrl)
                        .setShortUri(shortLinkSuffix)
                        .setEnableStatus(0).setFavicon(getFavicon(requestParam.getOriginUrl()));
        try{
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException e){
            //TODO 已经误判的短链接如何处理
            log.warn("短链接:{}重复入库",fullShortUrl);
            throw new ServiceException("短链接生成重复");
        }
        //路由表保存对应fullShortUrl的gid，好路由t_link表时找分片键gid
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid()).build();
        int insert = shortLinkGotoMapper.insert(shortLinkGotoDO);
        if(insert!=1){
            log.warn("短链接插入路由失败");
            throw new ClientException("创建失败");
        }
        shortLinkCreateCachePenetrationBloomFilter.add(shortLinkSuffix);
        stringRedisTemplate.opsForValue()
                .set(String.format(GOTO_SHORT_LINK_KEY
                        ,fullShortUrl)
                        ,shortLinkDO.getOriginUrl()
                        ,ShortLinkUtil.getValidDate(shortLinkDO.getValidData())
                        ,TimeUnit.MILLISECONDS);
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    private void validateUserShortLinkQuota() {
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new ClientException("用户未登录");
        }
        List<GroupDO> userGroups = linkGroupMapper.selectList(
                Wrappers.lambdaQuery(GroupDO.class)
                        .eq(GroupDO::getUsername, username)
                        .eq(GroupDO::getDelFlag, false)
        );
        if (userGroups == null || userGroups.isEmpty()) {
            return;
        }
        List<String> gidList = userGroups.stream().map(GroupDO::getGid).toList();
        Long shortLinkCount = shortLinkMapper.selectCount(
                Wrappers.lambdaQuery(ShortLinkDO.class)
                        .in(ShortLinkDO::getGid, gidList)
                        // 包含回收站（enable_status=1），只排除永久删除（del_flag=1）
                        .eq(ShortLinkDO::getDelFlag, 0)
        );
        if (shortLinkCount != null && shortLinkCount >= USER_MAX_SHORT_LINK_COUNT) {
            throw new ClientException("每个用户最多只能创建10条短链接（包含回收站）");
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage= baseMapper.selectPage(requestParam,eq);
        return resultPage.convert(item->BeanUtil.toBean(item,ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids) {
        return shortLinkMapper.listGroupShortLinkCount(gids);
    }

    //TODO 这里没有判断分组存在不存在，的admin去做判断
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO hasShortLinkDO  = baseMapper.selectOne(queryWrapper);
        if(hasShortLinkDO ==null){
            throw new ClientException("短链接不存在");
        }
        if(Objects.equals(hasShortLinkDO.getGid(),requestParam.getGid())){
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDataType(), ValidDataTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidData, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .createdType(hasShortLinkDO.getCreatedType())
                    .originUrl(requestParam.getOriginUrl())
                    .description(requestParam.getDescription())
                    .validDataType(requestParam.getValidDataType())
                    .validData(requestParam.getValidData())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        }else{
            LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(ShortLinkDO::getDelFlag, 1);
            baseMapper.update(linkUpdateWrapper);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .originUrl(requestParam.getOriginUrl())
                    .gid(requestParam.getGid())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .validDataType(requestParam.getValidDataType())
                    .validData(requestParam.getValidData())
                    .description(requestParam.getDescription())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .enableStatus(hasShortLinkDO.getEnableStatus())
                    .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                    .build();
            baseMapper.insert(shortLinkDO);
        }

    }
    private final ExecutorService statsExecutor;

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        String fullShortUrl="https"+"://"+createShortLinkDefaultDomain+"/"+shortUri;
        String originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        log.info("redis缓存获取原始链接：{}",originalUrl);
        if (StrUtil.isNotBlank(originalUrl)) {
            log.info("redis缓存不为空，准备跳转原始链接:{}",originalUrl);
            ShortLinkStatsMessageDTO statsMsg = setUv(fullShortUrl, request, response);
            statsExecutor.execute(() -> sendLinkStatsMessage(fullShortUrl, null, statsMsg));
            response.sendRedirect(originalUrl);
            return;
        }
        //布隆过滤器可能误判，导致请求一直打到数据库，需要再加一层缓存空值
        //布隆过滤器不存在就一定不存在，但是如果不小心把布隆过滤器数据删了，不就页面一直不存在了吗
        boolean contains = shortLinkCreateCachePenetrationBloomFilter.contains(shortUri);
        if(!contains){
            log.info("布隆过滤器中没用此短链接{}，返回not found",shortUri);
            response.sendRedirect("/page/notfound");
            return;
        }
        //防止缓存穿透
        String gotoShortLinkIsnull = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_IS_NULL, fullShortUrl));
        if(StrUtil.isNotBlank(gotoShortLinkIsnull)){
            log.info("缓存穿透生效，穿透短链接为:{}",shortUri);
            response.sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK, fullShortUrl));
        boolean locked = false;
        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);
            log.warn("短链接{}缓存可能失效，进入分布式锁兜底",shortUri);
            //没有获取到锁就进入兜底
            if(!locked){
                originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
                if (StrUtil.isNotBlank(originalUrl)){
                    ShortLinkStatsMessageDTO statsMsg = setUv(fullShortUrl, request, response);
                    statsExecutor.execute(() -> sendLinkStatsMessage(fullShortUrl, null, statsMsg));
                    response.sendRedirect(originalUrl);
                    return;
                }
                throw new ServiceException("系统繁忙");
            }
            //获取到锁了之后也可能是前一个获取到锁的线程在三秒内查询数据库添加缓存，释放了锁让我们抢到了
            originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalUrl)){
                log.info("{}分布式锁兜底成功，缓存重新生效",shortUri);
                ShortLinkStatsMessageDTO statsMsg = setUv(fullShortUrl, request, response);
                statsExecutor.execute(() -> sendLinkStatsMessage(fullShortUrl, null, statsMsg));
                response.sendRedirect(originalUrl);
                return;
            }
            LambdaQueryWrapper<ShortLinkGotoDO> wrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(wrapper);
            if(shortLinkGotoDO==null){
                response.sendRedirect("/page/notfound");
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_IS_NULL,fullShortUrl),"-",30,TimeUnit.MINUTES);
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getEnableStatus,0)
                    .eq(ShortLinkDO::getDelFlag,0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || (shortLinkDO.getValidData() != null && shortLinkDO.getValidData().before(new Date()))) {
                response.sendRedirect("/page/notfound");
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_IS_NULL,fullShortUrl),"-",30,TimeUnit.MINUTES);
                return;
            }
            stringRedisTemplate.opsForValue()
                    .set(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl)
                                ,shortLinkDO.getOriginUrl()
                                ,ShortLinkUtil.getValidDate(shortLinkDO.getValidData())
                                ,TimeUnit.MILLISECONDS);
            ShortLinkStatsMessageDTO statsMsg = setUv(fullShortUrl, request, response);
            statsExecutor.execute(() -> sendLinkStatsMessage(fullShortUrl, shortLinkDO.getGid(), statsMsg));
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            if (locked) { // 只判断是否成功拿到过锁，无需手动判断线程持有
                try {
                    lock.unlock(); // 依赖Redisson内置的安全校验
                } catch (Exception e) {
                    log.warn("释放短链接锁失败，fullShortUrl:{}", fullShortUrl, e); // 记录具体URL，便于排查
                }
            }
        }
    }

    private ShortLinkStatsMessageDTO setUv(String fullShortUrl,HttpServletRequest request,HttpServletResponse response){
        AtomicReference<String> uv = new AtomicReference<>();
        AtomicBoolean isNewUv = new AtomicBoolean(true);
        Cookie[] cookies = request.getCookies();

        if (ArrayUtil.isNotEmpty(cookies)) {
            // 有Cookie：读取UV
            Optional<Cookie> uvCookie = Arrays.stream(cookies)
                    .filter(each -> "short_uv".equals(each.getName()))
                    .findFirst();
            if (uvCookie.isPresent()) {
                String uvValue = uvCookie.get().getValue();
                uv.set(uvValue);
                Long added = stringRedisTemplate.opsForSet().add("short_link_uv_stats", fullShortUrl + uvValue);
                isNewUv.set(added != null && added > 0);
            } else {
                // 无UV Cookie：生成+设置（主线程同步执行）
                String newUv = UUID.randomUUID().toString();
                uv.set(newUv);
                Cookie cookie = new Cookie("short_uv", newUv);
                // 使用 host-only cookie，避免域名/IP/子域不一致导致浏览器拒收或不回传
                cookie.setPath("/");
                cookie.setMaxAge(365 * 24 * 60 * 60);
                response.addCookie(cookie); // 主线程执行，安全
                stringRedisTemplate.opsForSet().add("short_link_uv_stats", fullShortUrl + newUv);
            }
        } else {
            // 无Cookie：生成+设置（主线程同步执行）
            String newUv = UUID.randomUUID().toString();
            uv.set(newUv);
            Cookie cookie = new Cookie("short_uv", newUv);
            // 使用 host-only cookie，避免域名/IP/子域不一致导致浏览器拒收或不回传
            cookie.setPath("/");
            cookie.setMaxAge(365 * 24 * 60 * 60);
            response.addCookie(cookie); // 主线程执行，安全
            stringRedisTemplate.opsForSet().add("short_link_uv_stats", fullShortUrl + newUv);
        }

        return ShortLinkStatsMessageDTO.builder()
                .isNewUv(isNewUv.get())
                .uv(uv.get())
                .browser(UserAgentParserUtil.getBrowserFromRequest(request))
                .device(UserAgentParserUtil.getDeviceFromRequest(request))
                .network(UserAgentParserUtil.getNetworkTypeFromRequest(request))
                .os(UserAgentParserUtil.getOsFromRequest(request))
                .ip(IpUtil.getRealIp(request))
                .build();
    }

    // 新增：收集监控数据并发送消息
    private void sendLinkStatsMessage(String fullShortUrl, String gid,ShortLinkStatsMessageDTO statsMsg) {
        Date date=new Date();
        statsMsg.setFullShortUrl(fullShortUrl)
                .setGid(gid)
                .setAccessTime(date)
                .setWeekday(DateUtil.dayOfWeekEnum(date).getValue())
                .setHour(DateUtil.hour(date, true))
                .setEventId(UUID.randomUUID().toString());

        String jsonString = JSON.toJSONString(statsMsg);
        if (!statsMqEnabled) {
            writeStatsToRedisStream(fullShortUrl, statsMsg, jsonString);
            return;
        }
        RocketMQTemplate rocketMQTemplate = rocketMQTemplateProvider.getIfAvailable();
        if (rocketMQTemplate == null) {
            log.warn("stats.mq.enabled=true 但未发现 RocketMQTemplate，降级写入 Redis Stream，fullShortUrl:{}", fullShortUrl);
            writeStatsToRedisStream(fullShortUrl, statsMsg, jsonString);
            return;
        }
        try {
            // 发送重试由 RocketMQ 客户端参数 retry-times-when-send-failed 统一控制
            SendResult sendResult = rocketMQTemplate.syncSend("short-link-stats-topic", jsonString, 3000);
            log.info("监控消息发送成功，fullShortUrl:{} result:{}", fullShortUrl, sendResult.getSendStatus());
        } catch (Exception ex) {
            log.error("监控消息发送失败，fullShortUrl:{}", fullShortUrl, ex);
            writeStatsToRedisStream(fullShortUrl, statsMsg, jsonString);
        }
    }

    private final RedisScript<Void> script=RedisScript.of(
            new ClassPathResource("/lua/zaddSream.lua")
    );
    public final String QPS_ZSET_KEY="qps_zset_key";

    private void writeStatsToRedisStream(String fullShortUrl, ShortLinkStatsMessageDTO statsMsg, String jsonString) {
        try {
            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    StreamRecords.string(
                            Map.of(
                                    "payload", jsonString,
                                    "eventId", StrUtil.blankToDefault(statsMsg.getEventId(), "")
                            )
                    ).withStreamKey(STATS_STREAM_KEY)
            );
            //统计流的qps，动态的执行流的trim
            stringRedisTemplate.execute(script,List.of(QPS_ZSET_KEY));
            log.warn("降级写入Redis Stream成功，fullShortUrl:{} recordId:{}", fullShortUrl, recordId);
        } catch (Exception streamEx) {
            log.error("写入Redis Stream失败，失败的完整消息:{},失败原因:{}", jsonString, streamEx.getMessage());
            throw new ServiceException("监控消息发送失败");
        }
    }

    private void doLinkStats(String fullShortUrl,String gid,HttpServletRequest request,HttpServletResponse response){
        if(gid==null){
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl));
            if (shortLinkGotoDO==null){
                throw new ServiceException("服务器数据异常");
            }
            gid=shortLinkGotoDO.getGid();
        }
        AtomicReference<String> uv=new AtomicReference<>();

        //判断是否存在cookie，有cookie就用redis的set判断是否是第一次访问，set集合可以去重
        Runnable runnable=()-> {
            uv.set(UUID.randomUUID().toString());
            Cookie cookie = new Cookie("short_uv",uv.get());
            // 使用 host-only cookie + 根路径，保证整个站点请求都能稳定回传同一 UV
            cookie.setPath("/");
            cookie.setMaxAge(365 * 24 * 60 * 60);
            stringRedisTemplate.opsForSet().add("short_link_uv_stats",
                    fullShortUrl + uv.get());
            response.addCookie(cookie);
        };
        AtomicBoolean atomicBoolean=new AtomicBoolean(true);
        Cookie[] cookies = request.getCookies();
        if(ArrayUtil.isNotEmpty(cookies)){
            Arrays.stream(cookies)
                    .filter(each -> each.getName().equals("short_uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each->{
                        uv.set(each);
                        Long added = stringRedisTemplate.opsForSet().add("short_link_uv_stats",
                                fullShortUrl + each);
                        atomicBoolean.set(added!=null&&added>1L);
                    },runnable);
        }else {
            runnable.run();
        }
        //高德地图查ip
        String ip = IpUtil.getRealIp(request);
        Long added = stringRedisTemplate.opsForSet().add("short_link_uip_stats", ip);
        LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                .pv(1)
                .uv(atomicBoolean.get()?1:0)
                .uip(added!=null&&added>0?1:0)
                .gid(gid)
                .date(new Date())
                .hour(DateUtil.hour(new Date(), true))
                .weekday(DateUtil.dayOfWeekEnum(new Date()).getValue())
                .fullShortUrl(fullShortUrl)
                .build();
        //查完IP后落库
        shortLinkAccessStatsDOMapper.shortLinkStats(linkAccessStatsDO);
        String city = null;
        String province = null;
        String adcode = null;

        //高德地图通过ip查城市
        AmapIpLocationResult locationByIp =null;
        try {
            locationByIp = amapIpUtil.getLocationByIp(ip);
        } catch (Exception e) {
            log.warn("高德地图定位失败:{}",e.getMessage());
        }
        if (locationByIp!=null) {
            city = locationByIp.getCity();
            province = locationByIp.getProvince();
            adcode = locationByIp.getAdcode();
        }
        LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .city(StrUtil.isBlank(city)||Objects.equals(city, "[]") ?"未知":city)
                .province(StrUtil.isBlank(province)||Objects.equals(province, "[]") ?"未知":province)
                .ascode(StrUtil.isBlank(adcode)||Objects.equals(adcode, "[]") ?"未知":adcode)
                .gid(gid)
                .country("未知")
                .date(new Date())
                .build();
        //查完城市后落库
        linkLocalStatsMapper.shortLinkLocaleStats(linkLocaleStatsDO);

        //通过请求request查操作系统
        String os = UserAgentParserUtil.getOsFromRequest(request);
        LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder().os(os)
                .cnt(1)
                .date(new Date())
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .build();
        //查完操作系统后落库
        linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);

        //通过请求request查浏览器
        String browser = UserAgentParserUtil.getBrowserFromRequest(request);
        LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .browser(browser)
                .cnt(1)
                .gid(gid)
                .date(new Date())
                .build();
        //查完浏览器后落库
        linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);

        //通过请求request查设备
        String device = UserAgentParserUtil.getDeviceFromRequest(request);
        LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .cnt(1)
                .date(new Date())
                .device(device)
                .build();
        //查完设备后落库
        linkDeviceStatsMapper.shortLinkDeviceStats(linkDeviceStatsDO);

        //通过请求request查网络
        String network = UserAgentParserUtil.getNetworkTypeFromRequest(request);
        LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(new Date())
                .network(network)
                .cnt(1)
                .build();
        //查完网络后落库
        linkNetworkStatsMapper.shortLinkNetworkStats(linkNetworkStatsDO);

        LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                .ip(ip)
                .locale(StrUtil.isBlank(city)||city.equals("[]")?"未知":city)
                .network(network)
                .fullShortUrl(fullShortUrl)
                .browser(browser)
                .user(uv.get())
                .os(os)
                .device(device)
                .build();
        linkAccessLogsMapper.insert(linkAccessLogsDO);
    }

    /**
     * 根据原始路径生成短链接
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam){
        int count=0;
        String uri =null;
        String originUrl = requestParam.getOriginUrl();
        do {
            uri = HashUtil.hashToBase62(originUrl);
            if (!shortLinkCreateCachePenetrationBloomFilter.contains(uri)) {
                break;
            }
            originUrl+=System.currentTimeMillis();
        }while (count++<=10);
        if(count>10){
            throw new ServiceException("短链接频繁生成，请稍后再试");
        }
        return uri;
    }
    // 模拟浏览器的请求头（关键：解决 405/反爬）
    private static final Map<String, String> BROWSER_HEADERS = Map.of(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8",
            "Accept-Encoding", "gzip, deflate, br",
            "Connection", "keep-alive",
            "Upgrade-Insecure-Requests", "1"
    );

    private String getFavicon(String url) throws IOException {
        // 1. 补全协议头
        if (url == null || url.trim().isEmpty()) {
            return "URL 不能为空";
        }
        String targetUrl = url.trim();
        if (!targetUrl.startsWith("http")) {
            targetUrl = "https://" + targetUrl;
        }

        try {
            URL urlObj = new URL(targetUrl);
            // 2. 先尝试直接获取 /favicon.ico（跳过 HTML 解析，避免 405）
            String defaultFavicon = urlObj.getProtocol() + "://" + urlObj.getHost() + "/favicon.ico";
            if (isUrlAccessible(defaultFavicon)) {
                return defaultFavicon;
            }

            // 3. 尝试解析 HTML（兼容 405 状态码，强制解析）
            Document document = Jsoup.connect(targetUrl)
                    .headers(BROWSER_HEADERS) // 加浏览器请求头
                    .timeout(5000)
                    .ignoreHttpErrors(true) // 忽略 405/403 等错误，继续解析
                    .ignoreContentType(true)
                    .get();

            Elements faviconLinks = document.select("link[rel~=(?i)^(icon|shortcut icon|apple-touch-icon)$]");
            if (!faviconLinks.isEmpty()) {
                String faviconUrl = faviconLinks.first().attr("abs:href");
                return faviconUrl;
            } else {
                return defaultFavicon;
            }

        } catch (Exception e) {
            return "获取图标失败：" + e.getMessage();
        }
    }

    // 校验 URL 是否可访问（兼容 GET/HEAD 方法）
    private boolean isUrlAccessible(String checkUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(checkUrl).openConnection();
            // 加浏览器请求头
            BROWSER_HEADERS.forEach(conn::setRequestProperty);
            // 先试 HEAD 方法（更轻量），失败试 GET
            try {
                conn.setRequestMethod("HEAD");
            } catch (Exception e) {
                conn.setRequestMethod("GET");
            }
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setInstanceFollowRedirects(true); // 跟随重定向
            int code = conn.getResponseCode();
            // 2xx 成功，3xx 重定向都算可访问
            return code >= 200 && code < 400;
        } catch (Exception e) {
            return false;
        }
    }
}
