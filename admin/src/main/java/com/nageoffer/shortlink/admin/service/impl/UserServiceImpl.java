package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.constant.Contant;
import com.nageoffer.shortlink.admin.common.constant.RedisCacheConstant;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCode;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserLogoutReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import com.nageoffer.shortlink.admin.toolkit.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void Register(UserRegisterReqDTO requestParam) {
        if(hasUsername(requestParam.getUsername())){
            throw new ClientException(UserErrorCode.USER_EXIST);
        }
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY);
        boolean isLock = lock.tryLock();
        try {
            if (isLock){
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if(insert<0){
                    throw new ClientException(UserErrorCode.USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            }else {
                throw new ClientException(UserErrorCode.USER_EXIST);
            }
        }finally {
            lock.unlock();
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
        stringRedisTemplate.opsForValue().set(Contant.USER_LOGIN+userDO.getUsername(),token,30, TimeUnit.MINUTES);
        return BeanUtil.toBean(userDO,UserLoginRespDTO.class).setToken(token);
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        //TODO 验证当前登录用户是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam,UserDO.class),updateWrapper);
    }

    @Override
    public void logout(UserLogoutReqDTO requestParam) {
        String username = requestParam.getUsername();
        String token = requestParam.getToken();
        String redisToken = stringRedisTemplate.opsForValue().get(Contant.USER_LOGIN + username);
        if(redisToken==null||redisToken.isEmpty()||!redisToken.equals(token)){
            throw new ClientException(UserErrorCode.USER_DATA_ERROR);
        }
        stringRedisTemplate.delete(Contant.USER_LOGIN+username);
    }
}
