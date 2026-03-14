package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    /**
    *根据用户名查询用户信息
     */
    private final UserService userService;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @GetMapping("/api/short-link/v1/user/{username}")
    public ResponseEntity<Result<UserRespDTO>> getUserByUsername(@PathVariable("username") String username) throws ClientException {
        return ResponseEntity.status(200)
                .body(Result.success(userService.getUserByUsername(username)));
    }

    @GetMapping("/api/short-link/v1/has_username/")
    public ResponseEntity<Result<Boolean>> hasUsername(@RequestParam("username")String username){
        boolean contains = userRegisterCachePenetrationBloomFilter.contains(username);
        return ResponseEntity.ok(Result.success(contains?"用户名已存在":"用户名不存在",contains));
    }

    @PostMapping("/api/short-link/v1/user")
    public ResponseEntity<Result<Void>> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.Register(requestParam);
        return ResponseEntity.ok(Result.success("注册成功"));
    }
}
