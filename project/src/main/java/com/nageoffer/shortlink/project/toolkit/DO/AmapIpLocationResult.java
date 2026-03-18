package com.nageoffer.shortlink.project.toolkit.DO;

import lombok.Data;

/**
 * 高德IP定位接口响应实体类
 */
@Data
public class AmapIpLocationResult {

    /**
     * 状态值：1 表示成功，0 表示失败
     */
    private String status;

    /**
     * 状态说明：如 "OK" 表示成功，"INVALID USER_SIGNATURE" 表示签名错误等
     */
    private String info;

    /**
     * 状态码：10000 表示成功，其他为错误码
     */
    private String infocode;

    /**
     * 省份名称：如 "北京市"、"广东省"
     */
    private String province;

    /**
     * 城市名称：如 "北京市"、"深圳市"
     */
    private String city;

    /**
     * 城市编码（adcode）：如 "110000"（北京）、"440300"（深圳）
     */
    private String adcode;

    /**
     * 城市矩形区域坐标：格式为 "左下经度,左下纬度;右上经度,右上纬度"
     */
    private String rectangle;
}