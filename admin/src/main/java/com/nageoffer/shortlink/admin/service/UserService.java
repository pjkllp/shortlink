package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dto.req.*;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {


    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username) throws ClientException;

    /**
     * 布隆过滤器判断用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     * @param requestParam 用户注册请求参数
     */
    void Register(UserRegisterReqDTO requestParam);


    /**
     * 用户登录
     * @param userLoginReqDTO 用户登录请求参数
     * @return 用户登录响应参数
     */
    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    /**
     * 用户退出登录
     * @param requestParam 用户退出登录请求参数
     */
    void logout(UserLogoutReqDTO requestParam);

    void getCode(UserRegisterReqDTO request);

    /**
     * 修改用户
     * @param request
     * @return
     */
    void getReviseCode(UserReviseReqDTO request);

    void revise(UserReviseReqDTO request);
}
