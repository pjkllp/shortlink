package com.nageoffer.shortlink.project.config;

import com.nageoffer.shortlink.project.common.convention.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerConfig {

    @ExceptionHandler
    public Result<Void> exception(Exception e){
        return Result.fail(e.getMessage());
    }
}
