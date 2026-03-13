package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCode;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Override
    public Boolean hasUsername(String username) {
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        List<UserDO> userDOS = baseMapper.selectList(eq);
        if(userDOS.isEmpty()){
            return false;
        }else if(userDOS.size()==1){
            return true;
        }else {
            throw new ClientException("用户数据异常", UserErrorCode.USER_DATAEORRO);
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
}
