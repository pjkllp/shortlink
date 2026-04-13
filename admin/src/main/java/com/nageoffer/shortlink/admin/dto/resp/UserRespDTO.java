package com.nageoffer.shortlink.admin.dto.resp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nageoffer.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.Data;

/**
 * 用户返回参数响应
 */
@Data
public class UserRespDTO {
    @TableId
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    @JsonSerialize(using= PhoneDesensitizationSerializer.class)
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 是否管理员：1=是，0=否
     */
    private Integer isAdmin;

}
