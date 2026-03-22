package com.nageoffer.shortlink.admin.remote.dto.Req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.dao.entity.LinkAccessLogsDO;
import lombok.Data;

@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 起始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
