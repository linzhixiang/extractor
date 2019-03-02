package com.epc.extractor.extractor.hbbidcloud.service;

import com.epc.extractor.extractor.hbbidcloud.query.ListPageQuery;
import com.epc.extractor.extractor.hbbidcloud.vo.ZbxmPageInfo;
import com.epc.extractor.extractor.query.MainPageQuery;

import java.util.List;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-03 14:24
 * <p>@Author : hj
 */
public interface HbbidcloudExtractor {


    /**
     * 从首页查询要爬取的子页列表
     * @param mainPageQuery
     * @return
     */
    List<ListPageQuery> getChildPageUrl(MainPageQuery mainPageQuery);

    /**
     * 从首页查询要爬取的子页列表
     * @param pageQueryList
     * @return
     */
    List<String> getDetailPageUrl(List<ListPageQuery> pageQueryList);

    /**
     * 通过明细页面地址抓取页面详情
     * @param url
     * @return
     */
    ZbxmPageInfo getZbxmPageInfo(ZbxmPageInfo url);

}
