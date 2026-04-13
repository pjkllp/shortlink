package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.AccessLogDO;
import com.nageoffer.shortlink.project.dao.mapper.AccessLogMapper;
import com.nageoffer.shortlink.project.dto.Req.AccessLogPageReqDTO;
import com.nageoffer.shortlink.project.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitorServiceImpl extends ServiceImpl<AccessLogMapper, AccessLogDO> implements MonitorService {

    @Override
    public Page<AccessLogDO> pageAccessLog(AccessLogPageReqDTO requestParam) {
        LambdaQueryWrapper<AccessLogDO> wrapper = Wrappers.lambdaQuery(AccessLogDO.class)
                .eq(StrUtil.isNotBlank(requestParam.getProvince()), AccessLogDO::getProvince, requestParam.getProvince())
                .eq(StrUtil.isNotBlank(requestParam.getCity()), AccessLogDO::getCity, requestParam.getCity())
                .ge(requestParam.getStartTime() != null, AccessLogDO::getAccessTime, requestParam.getStartTime())
                .le(requestParam.getEndTime() != null, AccessLogDO::getAccessTime, requestParam.getEndTime())
                .orderByDesc(AccessLogDO::getAccessTime);
        return baseMapper.selectPage(requestParam, wrapper);
    }
}
