package com.epc.extractor.extractor.hbbidcloud.job;

import com.epc.extractor.extractor.hbbidcloud.query.ListPageQuery;
import com.epc.extractor.extractor.hbbidcloud.service.HbbidcloudExtractor;
import com.epc.extractor.extractor.hbbidcloud.service.impl.HbbidcloudExtractorImpl;
import com.epc.extractor.extractor.query.MainPageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-04 11:42
 * <p>@Author : hj
 */
@Component
public class HbbidcloudJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbbidcloudJob.class);

    @Autowired
    private HbbidcloudExtractor hbbidcloudExtractor;

    //   @Scheduled(cron = "0 0 0/1 * *  ? ")
    @Scheduled(cron = "0  0/1 * * * ? ")
    public void hbbidcloudExtractorTask() {
        LOGGER.info("执行了hbbidcloudExtractor.getChildPageUrl定时任务");
        MainPageQuery mainPageQuery = new MainPageQuery();
        mainPageQuery.setKeyWord("招标公告");
        List<ListPageQuery> listPageQueries =  hbbidcloudExtractor.getChildPageUrl(mainPageQuery);
        hbbidcloudExtractor.getDetailPageUrl(listPageQueries);
        LOGGER.info("结束hbbidcloudExtractor.getChildPageUrl定时任务");

    }

}
