package com.nageoffer.shortlink.project.common.constant;

/**
 * redis key常量类
 */
public class RedisKeyConstant {
    /**
     * 短链接跳转前缀key
     */
    public final static String GOTO_SHORT_LINK_KEY="short_link_goto_%s";

    /**
     * 短链接跳转锁key
     */
    public final static String LOCK_GOTO_SHORT_LINK="short_link_lock_goto_%s";
}
