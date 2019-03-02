package com.epc.extractor.extractor.hbbidcloud.service.impl;

import com.epc.extractor.extractor.hbbidcloud.query.ListPageQuery;
import com.epc.extractor.extractor.hbbidcloud.service.HbbidcloudExtractor;
import com.epc.extractor.extractor.hbbidcloud.vo.ZbxmPageInfo;
import com.epc.extractor.extractor.query.MainPageQuery;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>Description : extractor
 * <p>Date : 2019-01-03 14:49
 * <p>@Author : hj
 */
@Service
public class HbbidcloudExtractorImpl implements HbbidcloudExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbbidcloudExtractorImpl.class);

    private final static String baseUrl = "http://www.hbbidcloud.com";

    private static int size = 500000;

    /**
     * BloomFilter (布隆过滤器)原理：当一个元素被加入集合时，通过K个散列函数将这个元素映射成一个位数组中的K个点，把它们置为1。
     * 检索时，我们只要看看这些点是不是都是1就（大约）知道集合中有没有它了：
     * 如果这些点有任何一个0，则被检元素一定不在；如果都是1，则被检元素很可能在。
     */
    private static final BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), size);

    private static final int pageSize = 10;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public List<ListPageQuery> getChildPageUrl(MainPageQuery mainPageQuery) {
        List<ListPageQuery> urls = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("http://www.hbbidcloud.com/hubei/tzgg/about.html").get();
            Elements firstDiv = doc.select(".wb-data-item");
            LOGGER.debug("firstDiv-> "+ firstDiv);
            Elements alist = firstDiv.select("a");
            for(Element ea:alist){
                String text = ea.text();
                LOGGER.debug("ea.text()-> "+text);
                if(StringUtils.isNotBlank(text) && text.equals(mainPageQuery.getKeyWord())){
                    String eaUrl = ea.attr("href");
                    LOGGER.debug("ea.eaUrl()-> "+eaUrl);
                    //获取待抓取页面的主页地址
                    String mainUrl = baseUrl+eaUrl;
                    Document docMain = Jsoup.connect(mainUrl).get();
                    Elements newFont = docMain.select("font[color='#ffffff']");
                    LOGGER.debug("newsHeadlines -> "+newFont);
                    Elements newTable = newFont.parents().get(1).nextElementSibling().children().select("table");
                    LOGGER.debug("newTable -> "+newTable);
                    for(Element tr:newTable.select("tr")){
                        if(tr.select("a").size() > 0){
                            String title = tr.select("font").text();
                            String partUrl = tr.select("a").attr("href");
                            LOGGER.debug("title & partUrl  -> "+title+" "+partUrl );
                            ListPageQuery query = new ListPageQuery();
                            query.setTitle(title);
                            query.setUrl(baseUrl+partUrl+"/?Paging=");
                            query.setPageNum(1);
                            query.setBlock(mainPageQuery.getKeyWord());
                            urls.add(query);
                        }
                    }
                }
            }
            return urls;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<String> getDetailPageUrl(List<ListPageQuery> pageQueryList) {
        if(!CollectionUtils.isEmpty(pageQueryList)){
            //根据不同的类别进行循环
            for(ListPageQuery listPageQuery:pageQueryList ){
                Document doc = null;
                try {
                    boolean isBreak = false;
                    ZbxmPageInfo zbxmPageInfo = getLastZbxmPageInfo(listPageQuery.getTitle());
                    //根据分页页码进行循环
                    for (int i = 1; i < pageSize; i++) {
                        if(isBreak)
                            break;
                        doc = Jsoup.connect(listPageQuery.getUrl()+i).get();
                        Elements firstUl = doc.select(".ewb-tnews-items2");
                        //根据每页的明细内容进行循环
                        for(Element ea :firstUl.select("a")){
                            try {

                                String text = ea.attr("title");
                                String href = ea.attr("href");
                                String createAt = ea.nextElementSibling().text();
                                String area = text.substring(text.indexOf("【")+1,text.indexOf("】"));
                                String infoState = ea.nextElementSibling().nextElementSibling().text();
                                LOGGER.info("title & area & text & href -> "+"第"+i+"页:"+listPageQuery.getTitle()+" "+area+" "+text+" "+href+" "+createAt+" "+infoState);
                                int textHashCode = text.hashCode();
                                LOGGER.debug("text.hashCode -> "+textHashCode);
                                //这里做重复插入筛选
                                if (bloomFilter.mightContain(textHashCode)) {
                                    LOGGER.error("重复数据，无需提交");
                                    isBreak = true;
                                    break;
                                }
                                //数据库最新数据和抓取数据一致就退出
                                if(zbxmPageInfo != null && zbxmPageInfo.getTitle().hashCode() == textHashCode){
                                    LOGGER.error("重复数据，无需提交");
                                    isBreak = true;
                                    break;
                                }
                                //抓取数据的日期比数据库的数据日期时间早，同样退出循环
                                if(zbxmPageInfo != null && zbxmPageInfo.getInfoCreateAt() != null && createAt != null
                                        && DateUtils.parseDate(zbxmPageInfo.getInfoCreateAt(),"yyyy-MM-dd").compareTo(
                                        DateUtils.parseDate(createAt,"yyyy-MM-dd") ) > 0){
                                    LOGGER.error("重复数据，无需提交");
                                    isBreak = true;
                                    break;
                                }
                                ZbxmPageInfo info = getZbxmPageInfo(area,text,listPageQuery.getTitle(),baseUrl+href,createAt,listPageQuery.getBlock(),infoState);
                                this.getZbxmPageInfo(info);
                            }catch (RuntimeException r){
                                r.printStackTrace();
                                LOGGER.error("",r);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error("",e);
                } catch (ParseException p){
                    p.printStackTrace();
                    LOGGER.error("",p);
                }
            }
        }
        return null;
    }

    private ZbxmPageInfo getZbxmPageInfo(String area,String title,String type,String url ,String createAt,String block,String infoState){
        ZbxmPageInfo info = new ZbxmPageInfo();
        info.setArea(area);
        info.setCreateAt(DateFormatUtils.format(new Date(),"yyyy-MM-dd hh:mm:ss"));
        info.setTitle(title);
        info.setType(type);
        info.setUrl(url);
        info.setInfoCreateAt(createAt);
        info.setBlock(block);
        info.setInfoState(infoState);
        return info;
    }

    private static final String ZBXMPAGEINFO = "ZbxmPageInfo";

    public ZbxmPageInfo getLastZbxmPageInfo(String type ) {
        //创建查询对象
        Query query = new Query();
        //设置排序
//        query.with(Sort.by(Sort.Direction.DESC,"infoCreateAt"));  这种写法只有2.0版本的springboot才支持
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "infoCreateAt")));
        //设置查询条数
        query.limit(1);
        //设置查询条件
        Criteria criteria = new Criteria().and("type").is(type);
        query.addCriteria(criteria);
        //查询当前页数据集合
        List<ZbxmPageInfo> flowEntities = null;
        try {
            flowEntities = mongoTemplate.find(query, ZbxmPageInfo.class, ZBXMPAGEINFO);
        } catch (Exception e) {
            LOGGER.error("mongoDB连接失败 getLastZbxmPageInfo e={}", e);
        }
        if(flowEntities.isEmpty())
            return null;
        return flowEntities.get(0);
    }

    @Override
    public ZbxmPageInfo getZbxmPageInfo(ZbxmPageInfo info) {
        if(info != null && StringUtils.isNotBlank(info.getUrl())){
            LOGGER.debug("getZbxmPageInfo -> "+info.toString());
            Document doc = null;
            try {
                /**
                 * jsoup 是一款Java 的HTML解析器，可直接解析某个URL地址、HTML文本内容。它提供了一套非常省力的API，可通过DOM，CSS以及类似于jQuery的操作方法来取出和操作数据。
                 */
                doc = Jsoup.connect(info.getUrl()).get();
                Elements firstTd = doc.select(".infodetail");
//                LOGGER.debug("firstTd -> "+firstTd.get(0));
                info.setPageInfo(firstTd.get(0).toString());
                //满足条件的数据插入到数据库
                mongoTemplate.save(info,"ZbxmPageInfo");
                bloomFilter.put(info.getTitle().hashCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
