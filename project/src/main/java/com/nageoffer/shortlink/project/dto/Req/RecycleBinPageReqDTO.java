package com.nageoffer.shortlink.project.dto.Req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;
import java.util.List;

/**
 * 回收站分页请求参数
 */
@Data
public class RecycleBinPageReqDTO extends Page<ShortLinkDO> {
    /**
     * 分组标识
     */
    private List<String> gidList;
}
