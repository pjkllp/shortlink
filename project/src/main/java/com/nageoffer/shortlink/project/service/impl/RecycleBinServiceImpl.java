package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.constant.RedisKeyConstant;
import com.nageoffer.shortlink.project.common.exceptions.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_IS_NULL;
import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    @Override
    public void recycleBinSave(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> set = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO build = ShortLinkDO.builder().enableStatus(1).build();
        stringRedisTemplate
                .delete(String.format(GOTO_SHORT_LINK_KEY,requestParam.getFullShortUrl()));
        baseMapper.update(build, set);
    }

    @Override
    public Page<RecycleBinPageRespDTO> recycleBinPage(RecycleBinPageReqDTO requestParam) {
        Page<ShortLinkDO> page = baseMapper.selectPage(
                new Page<>(requestParam.getCurrent(), requestParam.getSize()),
                Wrappers.lambdaQuery(ShortLinkDO.class)
                        .in(ShortLinkDO::getGid, requestParam.getGidList())
                        .eq(ShortLinkDO::getEnableStatus, 1)
                        .eq(ShortLinkDO::getDelFlag, 0)
        );
        return (Page<RecycleBinPageRespDTO>) page.convert(each->BeanUtil.toBean(each,RecycleBinPageRespDTO.class));
    }

    @Override
    public void recycleBinRecover(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl());
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().enableStatus(0).build();
        baseMapper.update(shortLinkDO,updateWrapper);
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY
                ,requestParam.getFullShortUrl()),requestParam.getOriginUrl());
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_IS_NULL,requestParam.getFullShortUrl()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recycleBinRemove(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl());
        int delete = baseMapper.delete(updateWrapper);
        LambdaUpdateWrapper<ShortLinkGotoDO> doLambdaUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkGotoDO::getGid, requestParam.getGid());
        int delete1 = shortLinkGotoMapper.delete(doLambdaUpdateWrapper);
        if(delete1!=1||delete!=1){
            throw new ServiceException("删除失败");
        }
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY,requestParam.getFullShortUrl()));
    }
}
