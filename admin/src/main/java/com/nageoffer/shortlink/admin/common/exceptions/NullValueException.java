package com.nageoffer.shortlink.admin.common.exceptions;

import com.nageoffer.shortlink.admin.common.convention.errorcode.IErrorCode;

public class NullValueException extends Exception implements IErrorCode {

    String code;
    String message;
    public NullValueException(String message) {
        super(message);
    }

    public NullValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullValueException(Throwable cause) {
        super(cause);
    }

    protected NullValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NullValueException() {
    }

    public NullValueException(String code,String message){
        this.code=code;
        this.message=message;
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
