package com.deer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.deer.pojo.Item;
import com.deer.repository.ItemRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    /**
     * 操作方式一
     */
    @Autowired
    private ElasticsearchRestTemplate template;


    /**
     * 操作方式二
     */
    @Resource
    private ItemRepository itemRepository;


    /**
     * template增加索引
     */
    public boolean addIndex() {
        boolean index = template.createIndex(Item.class);
        template.putMapping(Item.class);
        return index;
    }

    /**
     * template删除索引
     */
    public boolean deleteIndex() {
        return template.deleteIndex(Item.class);
    }

    /**
     * template查询映射关系
     */
    public Map<String, Object> findMapper() {
        return template.getMapping(Item.class);
    }

    /**
     * template索引文档
     */
    public String addDoc(Item item) {
        IndexQuery indexQuery = new IndexQueryBuilder().withId(item.getId().toString()).withObject(item).build();
        return template.index(indexQuery);
    }


    /**
     * 删除文档
     *
     * @param item
     * @return
     */
    public String delDoc(Item item) {
        return template.delete(item.getClass(), item.getId().toString());
    }

    /**
     * template查询文档
     * 按照id 查询
     *
     * @param id
     * @return
     */
    public Item fingDocById(String id) {
        return template.queryForObject(GetQuery.getById(id), Item.class);
    }

    /**
     * template 查询所有的文档 默认返回前十条
     *
     * @return
     */
    public List<Item> findAll() {
        SearchQuery query = new NativeSearchQueryBuilder().build();
        return template.queryForList(query, Item.class);
    }

    public List<Item> matchQuery(String title) {
        //构建查询器,封装查询条件
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("title", title);
        NativeSearchQuery builder = new NativeSearchQueryBuilder().withQuery(matchQuery).build();
        List<Item> items = template.queryForList(builder, Item.class);
        return items;
    }


    public List<Item> findPageAndSort(String title) {
        //1.构建查询器,封装查询条件
        MatchQueryBuilder builder = QueryBuilders.matchQuery("title", title);
        //2.分页查询器,封装分页条件
        PageRequest pageRequest = PageRequest.of(0, 10);
        return null;
    }


    public AggregatedPage<Item> highLightQuery(String title) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("title", title)).
                withHighlightBuilder(
                        new HighlightBuilder().
                                field("title").
                                preTags("<h1>").postTags("</h1>")
                ).build();
        AggregatedPage<Item> items = template.queryForPage(query, Item.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                SearchHit[] hitsHits = hits.getHits();
                List<T> list = new ArrayList<>();
                for (SearchHit hitsHit : hitsHits) {
                    T t = this.mapSearchHit(hitsHit, aClass);
                    System.out.println(t);
                    list.add(t);
                }
                return (AggregatedPage) new AggregatedPageImpl(list);
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {

                //高亮数据
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

                for (String s : highlightFields.keySet()) {

                    Text[] fragments = highlightFields.get(s).getFragments();
                    for (Text fragment : fragments) {
//                        System.out.println("fragment----------"+fragment.toString());
                        sourceAsMap.put("title", fragment.toString());
                    }
                }

                //普通数据
                String string = sourceAsMap.toString();
//                System.out.println("测试"+string);

//                System.out.println("map"+sourceAsMap);
                JSONObject jsonObject = new JSONObject(sourceAsMap);
//                System.out.println("jsonObject----"+jsonObject.toString());
                T t1 = JSON.parseObject(jsonObject.toString(), aClass);
//                System.out.println("ttttttt"+t1);
                return t1;
            }
        });

        return items;
    }


}
