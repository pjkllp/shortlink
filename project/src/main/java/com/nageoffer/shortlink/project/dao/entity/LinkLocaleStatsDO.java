package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接地域访问统计实体类
 * 对应表：t_link_local_stats
 */
@Data
@TableName("t_link_local_stats") // 明确指定数据库表名
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkLocaleStatsDO {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID) // 雪花算法生成ID（适配你之前的短链接ID策略）
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 删除标识（0-未删，1-已删）
     */
    private Boolean delFlag;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 国家标识
     */
    private String country;

    /**
     * 地区编码
     */
    private String ascode;
}