package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 短链接路由表实体
 */
@Data
@TableName("t_link_goto")
@Accessors(chain = true)
@Builder
public class ShortLinkGotoDO {
    /**
     * ID
     */

    private long id;
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
