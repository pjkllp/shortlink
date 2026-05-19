package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dto.req.*;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1")
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
    @GetMapping("/user/{username}")
    public ResponseEntity<Result<UserRespDTO>> getUserByUsername(@PathVariable("username") String username) throws ClientException {
        return ResponseEntity.status(200)
                .body(Result.success(userService.getUserByUsername(username)));
    }

    /**
     * 判断用户名是否已存在
     * @param username 用户名
     * @return 返回boolean，true为存在，false为不存在
     */
    @GetMapping("/has_username/")
    public ResponseEntity<Result<Boolean>> hasUsername(@RequestParam("username")String username){
        boolean contains = userRegisterCachePenetrationBloomFilter.contains(username);
        return ResponseEntity.ok(Result.success(contains?"用户名已存在":"用户名不存在",contains));
    }

    /**
     * 用户注册时，填写用户名密码还有qq邮箱后，需要获取验证码，验证码由qq邮箱发送
     */
    @PostMapping("/getCode")
    public Result<Void> getCode(@RequestBody UserRegisterReqDTO request){
        userService.getCode(request);
        return Result.success("验证码已发送到您邮箱！");
    }

    /**
     * 注册用户
     * @param requestParam 用户注册请求实体
     * @return 返回注册成功参数
     */
    @PostMapping("/user")
    public ResponseEntity<Result<Void>> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.Register(requestParam);
        return ResponseEntity.ok(Result.success("注册成功"));
    }


    /**
     * 用户登录
     * @param requestParam 用户登录请求实体
     * @return 用户登录响应实体
     */
    @PostMapping("/login")
    public ResponseEntity<Result<UserLoginRespDTO>> login(@RequestBody UserLoginReqDTO requestParam){
        UserLoginRespDTO respParam = userService.login(requestParam);
        return ResponseEntity.ok((Result.success("登录成功",respParam)));
    }

    /**
     * 刷新登录令牌接口
     */
    @PostMapping("/refreshLogin")
    public Result<String> refreshLogin(@RequestBody UserRefreshReqDTO requestParam){
        String token = userService.refreshLogin(requestParam);
        return Result.success("刷新登录成功,重新发送登录请求",token);
    }

    /**
     * 修改用户密码
     * @param request
     * @return
     */
    @GetMapping("/reviseCode")
    public Result<Void> getReviseCode(@RequestBody UserReviseReqDTO request){
        userService.getReviseCode(request);
        return Result.success("验证码发送成功");
    }

    /**
     * 修改验证码成功
     * @param
     * @return
     */
    @PutMapping("/revise")
    public Result<Void> revise(@RequestBody UserReviseReqDTO request){
        userService.revise(request);
        return Result.success("修改密码成功");
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Result<Void>> logout(@RequestBody UserLogoutReqDTO requestParam){
        userService.logout(requestParam);
        return ResponseEntity.ok(Result.success("退出登录成功"));
    }
}
