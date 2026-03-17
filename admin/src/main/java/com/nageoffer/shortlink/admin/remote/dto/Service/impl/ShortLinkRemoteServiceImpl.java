package com.nageoffer.shortlink.admin.remote.dto.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Service.ShortLinkRemoteService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
@Service
public class ShortLinkRemoteServiceImpl implements ShortLinkRemoteService {

    String STR="http://localhost:8001/api/short-link/project/v1";
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam){
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("gid",requestParam.getGid());
        requestMap.put("current",requestParam.getCurrent());
        requestMap.put("size",requestParam.getSize());
        String post = HttpUtil.post(STR + "/page", JSON.toJSONString(requestMap));
        Result<IPage<ShortLinkPageRespDTO>> bean = JSONUtil.toBean(post, Result.class);
        return bean;
    }

    @Override
    public Result<String> getTitle(String url) {
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("url",url);
        String get = HttpUtil.get("http://localhost:8001/api/short-link/title", requestMap);
        return JSON.parseObject(get, new TypeReference<Result<String>>() {
        });

    }

    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        Map<String, Object> map = BeanUtil.beanToMap(requestParam);
        String post = HttpUtil.post(STR + "/link", JSON.toJSONString(map));
        Result<ShortLinkCreateRespDTO> bean = JSONUtil.toBean(post, Result.class);
        return bean;
    }

    public List<ShortLinkGroupCountQueryRespDTO> count(@RequestParam List<String> requestParam){
        Map<String, Object> requestParam1 = MapUtil.of("requestParam", requestParam);
        String result = HttpUtil.get(STR + "/count", requestParam1);
        Result<List<ShortLinkGroupCountQueryRespDTO>> bean = JSON.parseObject(result,new TypeReference<Result<List<ShortLinkGroupCountQueryRespDTO>>>(){});
        return bean.getData();
    }


}
