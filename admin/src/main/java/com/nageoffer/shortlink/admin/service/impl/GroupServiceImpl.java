package com.nageoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGeneratorUtil;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void saveGroup(String groupName) {
        GroupDO groupDO = GroupDO.builder()
                .gid(generateGroupName())
                .sort_order(0)
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    private String generateGroupName(){
        String groupId =null;
        GroupDO groupDO=null;
        do{
            groupId = RandomGeneratorUtil.generateDigitCode();
            LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, groupId);
                    //TODO 设置用户名
//                    .eq(GroupDO::getUsername, null);
            groupDO = baseMapper.selectOne(eq);
        }while (groupDO!=null);
        return groupId;
    }
}
