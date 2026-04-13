package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.AccessLogDO;
import com.nageoffer.shortlink.project.dto.Req.AccessLogPageReqDTO;

public interface MonitorService extends IService<AccessLogDO> {

    /**
     * 访问日志分页：默认按 access_time 倒序；可选 startTime/endTime 区间
     */
    Page<AccessLogDO> pageAccessLog(AccessLogPageReqDTO requestParam);
}
