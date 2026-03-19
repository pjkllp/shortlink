package com.nageoffer.shortlink.project.common.biz.user;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

/**
 * 用户上下文
 */
public final class UserContext {
    
    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户至上下文
     *
     * @param user 用户详情信息
     */
    public static void setUser(UserInfo user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取上下文中用户 ID
     *
     * @return 用户 ID
     */
    public static String getUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getUserId).orElse(null);
    }

    /**
     * 获取上下文中用户名称
     *
     * @return 用户名称
     */
    public static String getUsername() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getUsername).orElse(null);
    }

    /**
     * 获取上下文中用户真实姓名
     *
     * @return 用户真实姓名
     */
    public static String getRealName() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getRealName).orElse(null);
    }

    /**
     * 清理用户上下文
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}