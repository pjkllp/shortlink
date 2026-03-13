package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
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
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @GetMapping("/api/shortlink/v1/user/{username}")
    public ResponseEntity<Result<UserRespDTO>> getUserByUsername(@PathVariable("username") String username) throws ClientException {
        return ResponseEntity.status(200)
                .body(Result.success(userService.getUserByUsername(username)));
    }

    @GetMapping("/api/shortlink/v1/has_username/{username}")
    public ResponseEntity<Result<Boolean>> hasUsername(@PathVariable("username")String username){
        return ResponseEntity.ok(Result.success(userRegisterCachePenetrationBloomFilter.contains(username)));
    }
}
