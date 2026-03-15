package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserLogoutReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
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

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 返回用户实体
     * @throws ClientException 客户端异常
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public ResponseEntity<Result<UserRespDTO>> getUserByUsername(@PathVariable("username") String username) throws ClientException {
        return ResponseEntity.status(200)
                .body(Result.success(userService.getUserByUsername(username)));
    }

    /**
     * 判断用户名是否已存在
     * @param username 用户名
     * @return 返回boolean，true为存在，false为不存在
     */
    @GetMapping("/api/short-link/admin/v1/has_username/")
    public ResponseEntity<Result<Boolean>> hasUsername(@RequestParam("username")String username){
        boolean contains = userRegisterCachePenetrationBloomFilter.contains(username);
        return ResponseEntity.ok(Result.success(contains?"用户名已存在":"用户名不存在",contains));
    }

    /**
     * 注册用户
     * @param requestParam 用户注册请求实体
     * @return 返回注册成功参数
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public ResponseEntity<Result<Void>> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.Register(requestParam);
        return ResponseEntity.ok(Result.success("注册成功"));
    }

    /**
     * 修改用户
     * @param requestParam 修改用户实体
     * @return 返回修改参数
     */
    @PutMapping("/api/short-link/admin/v1/user")
    public ResponseEntity<Result<Void>> update(@RequestBody UserUpdateReqDTO requestParam){
        userService.update(requestParam);
        return ResponseEntity.ok(Result.success());
    }

    /**
     * 用户登录
     * @param requestParam 用户登录请求实体
     * @return 用户登录响应实体
     */
    @PostMapping("/api/short-link/admin/v1/login")
    public ResponseEntity<Result<UserLoginRespDTO>> login(@RequestBody UserLoginReqDTO requestParam){
        UserLoginRespDTO respParam = userService.login(requestParam);
        return ResponseEntity.ok((Result.success("登录成功",respParam)));
    }

    @DeleteMapping("/api/short-link/admin/v1/logout")
    public ResponseEntity<Result<Void>> logout(@RequestBody UserLogoutReqDTO requestParam){
        userService.logout(requestParam);
        return ResponseEntity.ok(Result.success("退出登录成功"));
    }
}
