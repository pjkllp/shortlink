package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupStatsReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkStatsReqDTO;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.HashMap;

public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {
    void shortLinkBrowserStats(LinkBrowserStatsDO linkBrowserStatsDO);

    /**
     * 根据短链接获取指定日期内浏览器监控数据
     */
    @Select("SELECT " +
            "    tlbs.browser, " +
            "    SUM(tlbs.cnt) AS count " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_browser_stats tlbs ON tl.full_short_url = tlbs.full_short_url " +
            "WHERE " +
            "    tlbs.full_short_url = #{fullShortUrl} " +
            "    AND tl.gid = #{gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = #{enableStatus} " +
            "    AND tlbs.date BETWEEN #{startDate} and #{endDate} " +
            "GROUP BY " +
            "    tlbs.full_short_url, tl.gid, tlbs.browser;")
    List<HashMap<String, Object>> listBrowserStatsByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内浏览器监控数据
     */
    @Select("SELECT " +
            "    tlbs.browser, " +
            "    SUM(tlbs.cnt) AS count " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_browser_stats tlbs ON tl.full_short_url = tlbs.full_short_url " +
            "WHERE " +
            "    tl.gid = #{gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = '0' " +
            "    AND tlbs.date BETWEEN #{startDate} and #{endDate} " +
            "GROUP BY " +
            "    tl.gid, tlbs.browser;")
    List<HashMap<String, Object>> listBrowserStatsByGroup( ShortLinkGroupStatsReqDTO requestParam);
}
