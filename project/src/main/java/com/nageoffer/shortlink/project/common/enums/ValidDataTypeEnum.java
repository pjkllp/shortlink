package com.nageoffer.shortlink.project.common.enums;

import lombok.Getter;

/**
 * 有效期类型
 */
@Getter
public enum ValidDataTypeEnum {

    PERMANENT(0,"永久有效"),
    CUSTOM(1,"自定义");

    private final Integer type;
    private final String message;

    ValidDataTypeEnum(Integer type, String message){
        this.type=type;
        this.message=message;
    }
}
