package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.enums.ValidDataTypeEnum;
import com.nageoffer.shortlink.project.common.exceptions.ClientException;
import com.nageoffer.shortlink.project.common.exceptions.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkPageReqTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.toolkit.ShortLinkUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口实现层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

    private final ShortLinkMapper shortLinkMapper;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;
    @Value("${short-link.protocol}")
    private String shortLinkProtocol;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) throws IOException {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(shortLinkProtocol)
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

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqTO requestParam) {
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

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        String serverName = request.getServerName();
        String scheme = request.getScheme();
        String fullShortUrl=scheme+"://"+serverName+"/"+shortUri;
        String originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalUrl)) {
            response.sendRedirect(originalUrl);
            return;
        }
        //布隆过滤器可能误判，导致请求一直打到数据库，需要再加一层缓存空值
        boolean contains = shortLinkCreateCachePenetrationBloomFilter.contains(shortUri);
        if(!contains){
            response.sendRedirect("/page/notfound");
            return;
        }
        String gotoShortLinkIsnull = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_IS_NULL, fullShortUrl));
        if(StrUtil.isNotBlank(gotoShortLinkIsnull)){
            response.sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK, fullShortUrl));
        boolean locked = false;
        try {
            locked = lock.tryLock(3,10, TimeUnit.SECONDS);
            //没有获取到锁就进入兜底
            if(!locked){
                originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
                if (StrUtil.isNotBlank(originalUrl)){
                    response.sendRedirect(originalUrl);
                    return;
                }
                throw new ServiceException("系统繁忙");
            }
            //获取到锁了之后也可能是前一个获取到锁的线程在三秒内查询数据库添加缓存，释放了锁让我们抢到了
            originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalUrl)){
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
                response.sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            if(locked&&lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
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
