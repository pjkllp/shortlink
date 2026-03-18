package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.exceptions.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;
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
    public IPage<RecycleBinPageRespDTO> recycleBinPage(RecycleBinPageReqDTO requestParam) {
        IPage<ShortLinkDO> page = baseMapper.selectPage(
                new Page<>(requestParam.getCurrent(), requestParam.getSize()),
                Wrappers.lambdaQuery(ShortLinkDO.class)
                        .in(ShortLinkDO::getGid, requestParam.getGidList())
                        .eq(ShortLinkDO::getEnableStatus, 1)
                        .eq(ShortLinkDO::getDelFlag, 0)
        );
        return page.convert(each->BeanUtil.toBean(each,RecycleBinPageRespDTO.class));
    }
}
