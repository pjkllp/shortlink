package com.nageoffer.shortlink.admin.common.convention.result;

import com.nageoffer.shortlink.admin.common.convention.errorcode.IErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局返回对象
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 5679018624309023727L;

    /**
     * 正确返回码
     */
    public static final String SUCCESS_CODE = "0";

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求id
     */
    private String requestId;

    public boolean isSuccess(){
        return SUCCESS_CODE.equals(code);
    }
    public static <T> Result<T> success(){
        return new Result<T>().setCode("0");
    }
    public static <T> Result<T> success(String message){
        return new Result<T>().setCode("0").setMessage(message);
    }

    public static <T> Result<T> success(String message,T data){
        return new Result<T>().setMessage(message).setData(data).setCode("0");
    }

    public static <T> Result<T> success(T data){
        return new Result<T>().setCode("0").setData(data);
    }

    public static <T> Result<T> fail(IErrorCode iErrorCode){
        return new Result<T>().setCode(iErrorCode.code()).setMessage(iErrorCode.message());
    }

    public static <T> Result<T> fail(String message){
        return new Result<T>().setMessage(message);
    }

    public static <T> Result<T> fail(T data,IErrorCode iErrorCode){
        return new Result<T>().setMessage(iErrorCode.message()).setCode(iErrorCode.code()).setData(data);
    }
}