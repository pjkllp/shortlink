package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.Comsumer.doMessage;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.service.ScheduledService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduledServiceImpl implements ScheduledService {

    private final DlqMessageMapper dlqMessageMapper;
    private final doMessage doMessage;

    @Scheduled(fixedDelay = 30000)
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.NOT_SUPPORTED)
    public void replayFailedMessages() {
        LambdaQueryWrapper<DlqMessageDO> queryWrapper = Wrappers.lambdaQuery(DlqMessageDO.class)
                .eq(DlqMessageDO::getStatus, 0)
                .le(DlqMessageDO::getRetryCount, 2);
        List<DlqMessageDO> dlqMessageDOS = dlqMessageMapper.selectList(queryWrapper);
        for(DlqMessageDO item:dlqMessageDOS){
            try {
                doMessage.onMessage(item.getMessageStr());
                LambdaUpdateWrapper<DlqMessageDO> updateWrapper = Wrappers.lambdaUpdate(DlqMessageDO.class)
                        .eq(DlqMessageDO::getDlqTopic, item.getDlqTopic())
                        .eq(DlqMessageDO::getEventId, item.getEventId())
                        .set(true,DlqMessageDO::getStatus,1)
                        .setSql("retry_count = retry_count + 1");
                dlqMessageMapper.update(null,updateWrapper);
            } catch (Exception e) {
                LambdaUpdateWrapper<DlqMessageDO> updateWrapper = Wrappers.lambdaUpdate(DlqMessageDO.class)
                        .eq(DlqMessageDO::getEventId, item.getEventId())
                        .eq(DlqMessageDO::getDlqTopic, item.getDlqTopic());
                int retry = item.getRetryCount() + 1;
                DlqMessageDO dlqMessageDO = DlqMessageDO.builder()
                        .retryCount(retry)
                        .errorMessage(e.getMessage()!=null&&e.getMessage().length()>1000? e.getMessage().substring(0,1000): e.getMessage())
                        .status(retry >= 3 ? 2 : 0)
                        .build();
                dlqMessageMapper.update(dlqMessageDO,updateWrapper);
                log.error("消息重放处理失败，原因：{}",e.getMessage());
            }
        }
    }


}
