package com.nageoffer.shortlink.admin.config;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Result<Object>> handleNullValueException(ClientException e){
        return ResponseEntity.status(400)
                .body(new Result<>().setMessage(e.errorMessage).setCode(e.errorCode));
    }
}
