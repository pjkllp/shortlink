package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.biz.user.UserContext;
import com.nageoffer.shortlink.project.common.exceptions.ClientException;
import com.nageoffer.shortlink.project.dao.entity.GroupDO;
import com.nageoffer.shortlink.project.dao.mapper.LinkGroupMapper;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.project.service.GroupService;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<LinkGroupMapper, GroupDO> implements GroupService {
    private static final int USER_MAX_GROUP_COUNT = 3;

    private final ShortLinkService shortLinkService;

    @Override
    public void saveGroup(String groupName) {
        String username = Optional.ofNullable(UserContext.getUsername())
                .orElseThrow(() -> new ClientException("用户未登录"));
        saveGroup(username, groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        validateGroupQuota(username);
        GroupDO groupDO = GroupDO.builder()
                .gid(generateGroupId(username))
                .sortOrder(0)
                .username(username)
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        String username = Optional.ofNullable(UserContext.getUsername())
                .orElseThrow(() -> new ClientException("用户未登录"));
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, false)
                .eq(GroupDO::getUsername, username)
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(queryWrapper);
        List<ShortLinkGroupCountQueryRespDTO> count = shortLinkService
                .listGroupShortLinkCount(groupDOS.stream().map(GroupDO::getGid).toList());
        return groupDOS.stream().map(item -> {
            ShortLinkGroupRespDTO bean = BeanUtil.toBean(item, ShortLinkGroupRespDTO.class);
            Optional<ShortLinkGroupCountQueryRespDTO> first = count.stream()
                    .filter(each -> Objects.equals(bean.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(each -> bean.setShortLinkCount(each.getShortLinkCount()));
            return bean;
        }).toList();
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReq requestParam) {
        String username = Optional.ofNullable(UserContext.getUsername())
                .orElseThrow(() -> new ClientException("用户未登录"));
        if (requestParam == null || StrUtil.isBlank(requestParam.getGid()) || StrUtil.isBlank(requestParam.getName())) {
            throw new ClientException("修改参数不完整");
        }
        String newGroupName = requestParam.getName().trim();
        if (newGroupName.length() > 32) {
            throw new ClientException("分组名称长度不能超过32个字符");
        }
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, username)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, false);
        GroupDO groupDO = baseMapper.selectOne(queryWrapper);
        if (groupDO == null) {
            throw new ClientException("分组不存在或无权限修改");
        }
        baseMapper.update(
                GroupDO.builder().name(newGroupName).build(),
                Wrappers.lambdaUpdate(GroupDO.class)
                        .eq(GroupDO::getUsername, username)
                        .eq(GroupDO::getGid, requestParam.getGid())
                        .eq(GroupDO::getDelFlag, false)
        );
    }

    @Override
    public void deleteGroup(String gid) {
        String username = Optional.ofNullable(UserContext.getUsername())
                .orElseThrow(() -> new ClientException("用户未登录"));
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, username);
        baseMapper.update(GroupDO.builder().delFlag(true).build(), updateWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        String username = Optional.ofNullable(UserContext.getUsername())
                .orElseThrow(() -> new ClientException("用户未登录"));
        requestParam.forEach(item -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(item.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getGid, item.getGid());
            baseMapper.update(groupDO, updateWrapper);
        });
    }

    private String generateGroupId(String username) {
        String groupId;
        GroupDO groupDO;
        do {
            groupId = RandomUtil.randomNumbers(6);
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, groupId)
                    .eq(GroupDO::getUsername, username);
            groupDO = baseMapper.selectOne(queryWrapper);
        } while (groupDO != null);
        return groupId;
    }

    private void validateGroupQuota(String username) {
        LambdaQueryWrapper<GroupDO> countWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, username)
                .eq(GroupDO::getDelFlag, false);
        Long groupCount = baseMapper.selectCount(countWrapper);
        if (groupCount != null && groupCount >= USER_MAX_GROUP_COUNT) {
            throw new ClientException("每个用户最多只能创建3个分组");
        }
    }
}
