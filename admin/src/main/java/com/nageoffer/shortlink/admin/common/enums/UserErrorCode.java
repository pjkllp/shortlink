package com.nageoffer.shortlink.admin.common.enums;

import com.nageoffer.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCode implements IErrorCode {

    USER_NULL("B000200","用户记录不存在"),
    USER_EXIST("B000201","用户记录已存在"),
    USER_DATAEORRO("B000301","用户数据异常");
    ;

    private final String code;
    private final String message;

    // 构造方法
    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
