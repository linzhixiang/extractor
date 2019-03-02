package com.epc.extractor.extractor.query;

import lombok.Data;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-03 14:27
 * <p>@Author : hj
 */
@Data
public class MainPageQuery {

    private String mainPageUrl;

    /**
     * 关键字
     */
    private String keyWord;
}
