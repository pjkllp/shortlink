package com.nageoffer.shortlink.admin.remote.dto.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.admin.common.constant.Constant;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Service.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
@Service
@RequiredArgsConstructor
public class ShortLinkRemoteServiceImpl implements ShortLinkRemoteService {

    private final GroupMapper groupMapper;

    String STR="http://localhost:8001/api/short-link/project/v1";

    String suffix="http://localhost:8001";
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam){
        Map<String, Object> requestMap = BeanUtil.beanToMap(requestParam);
        String post = HttpUtil.post(STR + "/page", JSON.toJSONString(requestMap));
        return JSON.parseObject(post, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>() {
        });
    }

    @Override
    public Result<String> getTitle(String url) {
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("url",url);
        String get = HttpUtil.get("http://localhost:8001/api/short-link/title", requestMap);
        return JSON.parseObject(get, new TypeReference<Result<String>>() {
        });

    }

    @Override
    public Result<Void> recycleBinSave(RecycleBinSaveReqDTO requestParam) {
        Map<String, Object> map = BeanUtil.beanToMap(requestParam);
        String post = HttpUtil.post("http://localhost:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(map));
        return JSON.parseObject(post,new TypeReference<>(){});
    }

    @Override
    public Result<IPage<RecycleBinPageRespDTO>> recycleBinPage(RecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, Constant.USER_MESSAGE.get())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOS = groupMapper.selectList(queryWrapper);
        List<String> gidList = groupDOS.stream().map(GroupDO::getGid).toList();
        requestParam.setGidList(gidList);
        Map<String, Object> map = BeanUtil.beanToMap(requestParam);
        String result = HttpUtil.post("http://localhost:8001/api/short-link/v1/recycle-bin/page", JSON.toJSONString(map));
        return JSON.parseObject(result, new TypeReference<Result<IPage<RecycleBinPageRespDTO>>>() {});
    }

    @Override
    public Result<Void> recycleBinRecover(RecycleBinRecoverReqDTO requestParam) {
        Map<String, Object> map = BeanUtil.beanToMap(requestParam);
        String post = HttpUtil.post(suffix + "/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(map));
        return JSON.parseObject(post, new TypeReference<Result<Void>>() {});
    }

    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        Map<String, Object> map = BeanUtil.beanToMap(requestParam);
        String post = HttpUtil.post(STR + "/link", JSON.toJSONString(map));
        return JSON.parseObject(post,new TypeReference<Result<ShortLinkCreateRespDTO>>(){});
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> count(@RequestParam List<String> requestParam){
        Map<String, Object> requestParam1 = MapUtil.of("requestParam", requestParam);
        String result = HttpUtil.get(STR + "/count", requestParam1);
        Result<List<ShortLinkGroupCountQueryRespDTO>> bean = JSON.parseObject(result,new TypeReference<Result<List<ShortLinkGroupCountQueryRespDTO>>>(){});
        return bean.getData();
    }


}
