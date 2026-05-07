package com.nageoffer.shortlink.project.dto.Req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.dao.entity.AccessLogDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 访问日志分页请求：继承 MP 的 Page，current/size 由查询参数绑定；
 * 可选按 access_time 区间、省份、城市筛选
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccessLogPageReqDTO extends Page<AccessLogDO> {

    /**
     * 省份（精确匹配，空则不过滤）
     */
    private String province;

    /**
     * 城市（精确匹配，空则不过滤）
     */
    private String city;

    /**
     * 开始时间（含），按访问时间 access_time 过滤
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间（含），按访问时间 access_time 过滤
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
