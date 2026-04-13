package com.nageoffer.shortlink.admin.common.constant;

public class Constant {


    public static final String USER_LOGIN="short-link:user:login:";

    /**
     * 登录会话内管理员标识（与 token 同 TTL，供网关透传下游）
     */
    public static final String USER_IS_ADMIN = "short-link:user:is-admin:";
}
