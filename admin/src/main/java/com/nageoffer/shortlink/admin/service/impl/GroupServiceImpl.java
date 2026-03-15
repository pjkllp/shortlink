package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.constant.Contant;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGeneratorUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void saveGroup(String groupName) {
        GroupDO groupDO = GroupDO.builder()
                .gid(generateGroupName())
                .sortOrder(0)
                .username(Contant.USER_MESSAGE.get())
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> groupDOLambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, Contant.USER_MESSAGE.get())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(groupDOLambdaQueryWrapper);
        return BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReq requestParam) {
        LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, Contant.USER_MESSAGE.get())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        baseMapper.update(GroupDO.builder().name(requestParam.getName()).build(),eq);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Contant.USER_MESSAGE.get());
        baseMapper.update(GroupDO.builder().delFlag(1).build(),eq);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(item->{
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(item.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, Contant.USER_MESSAGE.get())
                    .eq(GroupDO::getGid, item.getGid());
            baseMapper.update(groupDO,eq);
        });
    }

    private String generateGroupName(){
        String groupId =null;
        GroupDO groupDO=null;
        do{
            groupId = RandomGeneratorUtil.generateDigitCode();
            LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, groupId)
                    //TODO 设置用户名
                    .eq(GroupDO::getUsername, Contant.USER_MESSAGE.get());
            groupDO = baseMapper.selectOne(eq);

        }while (groupDO!=null);
        return groupId;
    }
}
