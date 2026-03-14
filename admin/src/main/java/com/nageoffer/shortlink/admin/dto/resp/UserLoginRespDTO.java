package com.nageoffer.shortlink.admin.dto.resp;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserLoginRespDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * Jwt令牌
     */
    private String token;
}
