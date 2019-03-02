package com.epc.extractor.extractor.hbbidcloud.vo;

import lombok.Data;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-03 14:41
 * <p>@Author : hj
 */
@Data
public class ZbxmPageInfo {


    /**
     * 抓取的页面地址
     */
    private String url;

    /**
     * 页面内容
     */
    private String pageInfo;

    /**
     * 页面内容创建时间
     */
    private String infoCreateAt;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 抓取类别
     */
    private String type;

    /**
     * 抓取信息区域
     */
    private String area;

    private String tag;

    /**
     * 系统创建时间
     */
    private String createAt;

    /**
     * 页面区块
     */
    private String block;

    /**
     * 抓取内容的状态
     */
    private String infoState;
}
