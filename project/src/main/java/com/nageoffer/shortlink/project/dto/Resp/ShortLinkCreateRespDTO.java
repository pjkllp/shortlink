package com.nageoffer.shortlink.project.dto.Resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 短链接创建返回对象
 */
@Data
@Builder
public class ShortLinkCreateRespDTO {
    /**
     * 分组信息
     */
    private  String gid;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
