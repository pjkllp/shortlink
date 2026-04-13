package com.nageoffer.shortlink.project.toolkit;

import cn.hutool.core.lang.hash.MurmurHash;

/**
 * HASH 工具类 - 固定生成6位短链接
 */
public class HashUtil {

    private static final char[] CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final int SIZE = CHARS.length;
    private static final int SHORT_LINK_LENGTH = 6;   // 固定生成6位短链接

    /**
     * 将数字转换为62进制字符串，并固定为6位长度
     * 不足6位前面补0，超过6位取前6位
     */
    private static String convertDecToBase62(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int i = (int) (num % SIZE);
            sb.append(CHARS[i]);
            num /= SIZE;
        }

        String result = sb.reverse().toString();

        // 固定返回6位短链接
        if (result.length() >= SHORT_LINK_LENGTH) {
            return result.substring(0, SHORT_LINK_LENGTH);
        } else {
            // 不足6位，前面补0
            return String.format("%0" + (SHORT_LINK_LENGTH - result.length()) + "d%s", 0, result);
        }
    }

    /**
     * 将字符串哈希后转为固定6位的62进制短链接
     */
    public static String hashToBase62(String str) {
        int i = MurmurHash.hash32(str);
        long num = i < 0 ? Integer.MAX_VALUE - (long) i : i;
        return convertDecToBase62(num);
    }
}