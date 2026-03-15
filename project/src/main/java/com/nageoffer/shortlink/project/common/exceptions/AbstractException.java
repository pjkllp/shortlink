package com.nageoffer.shortlink.project.common.exceptions;

import com.nageoffer.shortlink.project.common.convention.errorcode.IErrorCode;
import lombok.Getter;

/**
 * 抽象出业务中的三种异常体系，客户端异常，服务端异常，远程调用异常
 * @see  ClientException
 * @see  ServiceException
 * @see  RemoteException
 */
@Getter
public abstract class AbstractException extends RuntimeException{
    public final String errorCode;
    public final String errorMessage;

    //abstract修饰的类用来抽出子类共有属性并且赋值，子类必须显示调用
    public AbstractException(String message, Throwable throwable, IErrorCode iErrorCode) {
        super(message,throwable);
        this.errorCode = iErrorCode.code();
        this.errorMessage = iErrorCode.message();
    }
}
