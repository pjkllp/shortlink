package com.nageoffer.shortlink.gateway.common.exceptions;

import com.nageoffer.shortlink.gateway.common.convention.errorcode.BaseErrorCode;
import com.nageoffer.shortlink.gateway.common.convention.errorcode.IErrorCode;

public class ServiceException extends  AbstractException{
    public ServiceException(String message, Throwable throwable, IErrorCode iErrorCode) {
        super(message, throwable, iErrorCode);
    }
    public ServiceException(String message){
        this(message,null, BaseErrorCode.SERVICE_ERROR);
    }
    public ServiceException(IErrorCode iErrorCode){
        this(null,null,iErrorCode);
    }
    public ServiceException(String message,IErrorCode iErrorCode){
        this(message,null,iErrorCode);
    }
}
