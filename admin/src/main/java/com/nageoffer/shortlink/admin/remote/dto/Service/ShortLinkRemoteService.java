package com.nageoffer.shortlink.admin.remote.dto.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ShortLinkRemoteService{

    /**
     * 远程调用查询各分组短链接数量
     * @param requestParam 分组的集合
     * @return 分组数据实参
     */
    public List<ShortLinkGroupCountQueryRespDTO> count(@RequestParam List<String> requestParam);

    /**
     * 远程创建短链接
     * @param requestParam 远程创建短链接实参
     * @return
     */
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询参数
     */
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam);

    /**
     * 根据url查询网址标题
     * @param url 网址
     * @return 网址标题
     */
    public Result<String> getTitle(String url);
}
