package com.nageoffer.shortlink.admin.toolkit;

import java.security.SecureRandom;

/**
 * 分组ID随机生成器
 */
public class RandomGeneratorUtil {
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM=new SecureRandom();

    /**
     * 生成随机分组ID
     * @return 分组ID
     */
    public static String generateDigitCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成随机分组ID
     * @return 分组ID
     */
    public static String generateDigitCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }

}
