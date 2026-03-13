package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.Result;
import com.nageoffer.shortlink.admin.common.exceptions.NullValueException;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    /**
    *根据用户名查询用户信息
     */
    private final UserService userService;


    @GetMapping("/api/shortlink/v1/user/{username}")
    public ResponseEntity<Result<UserRespDTO>> getUserByUsername(@PathVariable("username") String username) throws NullValueException {
        return ResponseEntity.status(200)
                .body(new Result<UserRespDTO>().setCode("0").setData(userService.getUserByUsername(username)).setMessage("查询成功"));
    }
}
