package com.nageoffer.shortlink.admin.dto.req;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserReviseReqDTO {

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 新密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;
}
