package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.constant.Constant;
import com.nageoffer.shortlink.admin.common.constant.RedisCacheConstant;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCode;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.common.exceptions.ServiceException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.*;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.remote.Service.ShortLinkActualRemoteService;
import com.nageoffer.shortlink.admin.service.EmailService;
import com.nageoffer.shortlink.admin.service.UserService;
import com.nageoffer.shortlink.admin.toolkit.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.rmi.server.ServerCloneException;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.admin.service.impl.EmailServiceImpl.USER_REVISE_CODE_KEY;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final EmailService emailService;

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void Register(UserRegisterReqDTO requestParam) {
        notEmpty(requestParam);
        if(hasUsername(requestParam.getUsername())){
            throw new ClientException(UserErrorCode.USER_EXIST);
        }
        notExists(requestParam);
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY+requestParam.getUsername());
        boolean isLock = lock.tryLock();
        try {
            if (isLock){
                UserDO userDO = BeanUtil.toBean(requestParam, UserDO.class);
                userDO.setIsAdmin(0);
                if(StrUtil.isNotBlank(requestParam.getInvitationCode())
                        && requestParam.getInvitationCode().equals(stringRedisTemplate.opsForValue().get("invitation_code_key"))){
                    userDO.setIsAdmin(1);
                }
                int insert = baseMapper.insert(userDO);
                if(insert<0){
                    throw new ClientException(UserErrorCode.USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                ShortLinkGroupSaveReqDTO saveReqDTO = new ShortLinkGroupSaveReqDTO();
                saveReqDTO.setName("默认分组");
                shortLinkActualRemoteService.saveGroupForUsername(requestParam.getUsername(), "0", saveReqDTO);
            }else {
                throw new ClientException(UserErrorCode.USER_EXIST);
            }
        }finally {
            lock.unlock();
        }
    }

    private void notEmpty(UserRegisterReqDTO requestParam){
        if (requestParam == null
                || StrUtil.isBlank(requestParam.getUsername())
                || StrUtil.isBlank(requestParam.getPassword())
                || StrUtil.isBlank(requestParam.getMail())
        ) {
            throw new ClientException("所有注册参数均不能为空");
        }
    }

    @Override
    public UserRespDTO getUserByUsername(String username) throws ClientException {
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(eq);
        if(userDO==null){
            throw new ClientException(UserErrorCode.USER_NULL);
        }
        UserRespDTO userRespDTO=new UserRespDTO();
        BeanUtil.copyProperties(userDO,userRespDTO);
        return userRespDTO;
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> userDOLambdaQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        UserDO userDO = baseMapper.selectOne(userDOLambdaQueryWrapper);
        if(userDO==null){
            throw new ClientException(UserErrorCode.USER_NULL);
        }
        if(!userDO.getPassword().equals(requestParam.getPassword())){
            throw new ClientException(UserErrorCode.USER_PASSWORD_ERROR);
        }
        String token = JwtUtil.generateJwt(userDO.getUsername());
        stringRedisTemplate.opsForValue().set(Constant.USER_LOGIN+userDO.getUsername(),token,30, TimeUnit.MINUTES);
        String isAdminFlag = (userDO.getIsAdmin() != null && userDO.getIsAdmin() == 1) ? "1" : "0";
        stringRedisTemplate.opsForValue().set(Constant.USER_IS_ADMIN + userDO.getUsername(), isAdminFlag, 30, TimeUnit.MINUTES);
        return BeanUtil.toBean(userDO,UserLoginRespDTO.class).setToken(token);
    }

    @Override
    public void logout(UserLogoutReqDTO requestParam) {
        String username = requestParam.getUsername();
        String token = requestParam.getToken();
        String redisToken = stringRedisTemplate.opsForValue().get(Constant.USER_LOGIN + username);
        if(redisToken==null||redisToken.isEmpty()||!redisToken.equals(token)){
            throw new ClientException(UserErrorCode.USER_DATA_ERROR);
        }
        stringRedisTemplate.delete(Constant.USER_LOGIN+username);
        stringRedisTemplate.delete(Constant.USER_IS_ADMIN + username);
    }

    @Override
    public void getCode(UserRegisterReqDTO request) {
        notEmpty(request);
        notExists(request);
        emailService.sendEmail(request);
    }

    @Override
    public void getReviseCode(UserReviseReqDTO request) {
        if (request.getMail().isBlank()||request.getPassword().isBlank()){
            throw new ClientException("请将信息填写完整");
        }
        String username = UserContext.getUsername();
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(username.isBlank()||!userDO.getUsername().equals(username)){
            throw new ClientException("你的身份非法");
        }
        RLock lock = redissonClient.getLock("user_revise_key");
        boolean isLocked = lock.tryLock();
        if (!isLocked){
            throw new ClientException("请勿频繁操作");
        }
        try{
            if(!userDO.getMail().equals(request.getMail())){
                throw new ClientException("您的邮箱输入错误！");
            }
            emailService.sendReviseMail(request.getMail());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void revise(UserReviseReqDTO request) {
        String username = UserContext.getUsername();
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(username.isBlank()
                ||!userDO.getUsername().equals(username)
                ||request.getMail().equals(userDO.getMail())){
            throw new ClientException("你的身份非法");
        }
        if (!request.getCode().equals(stringRedisTemplate.opsForValue().get(String.format(USER_REVISE_CODE_KEY,request.getMail())))){
            throw new ClientException("验证码错误");
        }
        LambdaUpdateWrapper<UserDO> eq = Wrappers.lambdaUpdate(UserDO.class)
                .set(UserDO::getPassword,request.getPassword())
                .eq(UserDO::getUsername, username);
        int update = baseMapper.update(eq);
        if (update<1){
            throw new ServiceException("密码更新失败");
        }
    }

    private void notExists(UserRegisterReqDTO request){
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,request.getUsername())
                .or().eq(UserDO::getMail, request.getMail());
        UserDO userDO = baseMapper.selectOne(eq);
        if(userDO!=null){
            if (userDO.getUsername().equals(request.getUsername())) {
                throw new ClientException("用户名已存在！");
            }else {
                throw new ClientException("密码已存在！");
            }
        }
    }
}
