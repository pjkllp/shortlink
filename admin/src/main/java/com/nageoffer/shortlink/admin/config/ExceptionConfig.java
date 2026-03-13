package com.nageoffer.shortlink.admin.config;

import com.nageoffer.shortlink.admin.common.convention.Result;
import com.nageoffer.shortlink.admin.common.exceptions.NullValueException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionConfig {
    @ExceptionHandler(NullValueException.class)
    public ResponseEntity<Result<Object>> handleNullValueException(NullValueException e){
        return ResponseEntity.status(400)
                .body(new Result<>().setMessage(e.message()).setCode(e.code()));
    }
}
