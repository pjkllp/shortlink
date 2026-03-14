package com.nageoffer.shortlink.admin.common.enums;

import com.nageoffer.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCode implements IErrorCode {

    USER_NULL("B000200","用户名不存在"),
    USER_EXIST("B000201","用户名已经存在"),
    USER_RECORD_EXIST("B000203","用户记录已存在"),
    USER_DATAEORRO("B000301","用户数据异常"),
    USER_SAVE_ERROR("B000202","用户记录新增失败")
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
