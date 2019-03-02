package com.epc.extractor.extractor.hbbidcloud.query;

import com.epc.extractor.extractor.query.MainPageQuery;
import lombok.Data;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-03 14:38
 * <p>@Author : hj
 * 列表页面信息
 */
@Data
public class ListPageQuery extends MainPageQuery {

    /**
     * 列表页分页信息
     */
    private Integer pageNum;

    /**
     * 列表页title
     */
    private String title;

    /**
     * 列表页地址
     */
    private String url;

    /**
     * 页面区块
     */
    private String block;

}
