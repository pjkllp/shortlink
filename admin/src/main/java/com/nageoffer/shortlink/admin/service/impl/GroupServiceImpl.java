package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.constant.Constant;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Service.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final ShortLinkRemoteService shortLinkRemoteService;
    @Override
    public void saveGroup(String groupName) {
        GroupDO groupDO = GroupDO.builder()
                .gid(generateGroupName())
                .sortOrder(0)
                .username(UserContext.getUsername())
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public void saveGroup(String username, String groupMame) {
        GroupDO groupDO = GroupDO.builder()
                .gid(generateGroupName(username))
                .sortOrder(0)
                .username(username)
                .name(groupMame)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> groupDOLambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(groupDOLambdaQueryWrapper);
        List<ShortLinkGroupCountQueryRespDTO> count = shortLinkRemoteService
                .count(groupDOS.stream().map(GroupDO::getGid).toList());
        return groupDOS.stream().map(item -> {
            ShortLinkGroupRespDTO bean = BeanUtil.toBean(item, ShortLinkGroupRespDTO.class);
            Optional<ShortLinkGroupCountQueryRespDTO> first = count.stream().filter(each -> Objects.equals(bean.getGid(), each.getGid())).findFirst();
            first.ifPresent(each -> bean.setShortLinkCount(each.getShortLinkCount()));
            return bean;
        }).toList();
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReq requestParam) {
        LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        baseMapper.update(GroupDO.builder().name(requestParam.getName()).build(),eq);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        baseMapper.update(GroupDO.builder().delFlag(1).build(),eq);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(item->{
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(item.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
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
                    .eq(GroupDO::getUsername,UserContext.getUsername());
            groupDO = baseMapper.selectOne(eq);

        }while (groupDO!=null);
        return groupId;
    }
    private String generateGroupName(String username){
        String groupId =null;
        GroupDO groupDO=null;
        do{
            groupId = RandomGeneratorUtil.generateDigitCode();
            LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, groupId)
                    //TODO 设置用户名
                    .eq(GroupDO::getUsername, username);
            groupDO = baseMapper.selectOne(eq);

        }while (groupDO!=null);
        return groupId;
    }
}
