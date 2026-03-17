package com.nageoffer.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.SHORT_LINK_VALID_TIME;

public class ShortLinkUtil {

    /**
     * 获取短链接缓存有效时间
     * @param valid 有效期时间，如果是null则标识永久有效，这个时候默认缓存30天
     * @return 返回有效期时间
     */
    public static long getValidDate(Date valid){
        return Optional.ofNullable(valid).map(each-> DateUtil.between(new Date(),each, DateUnit.MS))
                .orElse(SHORT_LINK_VALID_TIME);
    }
}
