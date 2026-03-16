package com.nageoffer.shortlink.admin.remote.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    String STR="http://localhost:8001/api/short-link/project/v1";
    default String pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam){
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("gid",requestParam.getGid());
        requestMap.put("current",requestParam.getCurrent());
        requestMap.put("size",requestParam.getSize());
        return HttpUtil.post(STR+"/page",JSON.toJSONString(requestMap));
    }

    default String createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        Map<String, Object> map = BeanUtil.beanToMap(requestParam);
        return HttpUtil.post(STR + "/link", JSON.toJSONString(map));
    }
}
