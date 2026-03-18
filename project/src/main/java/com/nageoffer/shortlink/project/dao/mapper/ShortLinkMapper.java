package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(@Param("gids") List<String> gids);

    List<RecycleBinPageRespDTO> recycleBinPageReqDTO(List<String> gidList);
}
