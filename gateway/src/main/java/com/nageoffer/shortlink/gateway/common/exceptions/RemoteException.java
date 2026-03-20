package com.nageoffer.shortlink.gateway.common.exceptions;

import com.nageoffer.shortlink.gateway.common.convention.errorcode.BaseErrorCode;
import com.nageoffer.shortlink.gateway.common.convention.errorcode.IErrorCode;

public class RemoteException extends AbstractException{
    public RemoteException(String message, Throwable throwable, IErrorCode iErrorCode) {
        super(message, throwable, iErrorCode);
    }
    public RemoteException(String message){
        this(message,null, BaseErrorCode.REMOTE_ERROR);
    }
    public RemoteException(String message,IErrorCode iErrorCode){
        this(message,null,iErrorCode);
    }
    public RemoteException(IErrorCode iErrorCode){
        this(null,null,iErrorCode);
    }
}
