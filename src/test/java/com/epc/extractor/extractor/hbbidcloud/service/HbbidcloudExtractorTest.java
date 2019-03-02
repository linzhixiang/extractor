package com.epc.extractor.extractor.hbbidcloud.service;

import com.epc.extractor.extractor.hbbidcloud.query.ListPageQuery;
import com.epc.extractor.extractor.query.MainPageQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-03 15:20
 * <p>@Author : hj
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HbbidcloudExtractorTest {

    @Autowired
    private HbbidcloudExtractor hbbidcloudExtractor;

//    @Test
    public void testGetChildPageUrl(){
        MainPageQuery mainPageQuery = new MainPageQuery();
        mainPageQuery.setKeyWord("招标项目");
        List<ListPageQuery> listPageQueries =  hbbidcloudExtractor.getChildPageUrl(mainPageQuery);
        hbbidcloudExtractor.getDetailPageUrl(listPageQueries);
    }

//    @Test
    public void testGetChildPageUrl2(){
        MainPageQuery mainPageQuery = new MainPageQuery();
        mainPageQuery.setKeyWord("招标公告");
        List<ListPageQuery> listPageQueries =  hbbidcloudExtractor.getChildPageUrl(mainPageQuery);
        hbbidcloudExtractor.getDetailPageUrl(listPageQueries);
    }
}
