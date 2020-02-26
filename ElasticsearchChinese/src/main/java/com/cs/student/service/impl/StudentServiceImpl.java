package com.cs.student.service.impl;

import com.cs.student.model.StudentEntity;
import com.cs.student.service.StudentService;
import com.cs.student.utils.ElasticsearchUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cs
 * @date 2020/02/17
 */
@Slf4j
@Service
public class StudentServiceImpl implements StudentService {

    /**
     * Es工具类
     */
    private ElasticsearchUtils esUtils;
    @Autowired
    public void setEsUtils(ElasticsearchUtils esUtils){
        this.esUtils = esUtils;
    }

    /**
     * 拼接搜索条件
     *
     * @param field     default the name
     * @param content  查询内容
     * @return list
     */
    @Override
    public List<Map> search(String field, String content) {
        //使用中文拼音混合搜索
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(structureQuery(field, content));
        List<StudentEntity> studentEntityList =  esUtils.search(sourceBuilder,
                StudentEntity.class);
        List<Map> retListStudent = new ArrayList<>(16);
        if(!CollectionUtils.isEmpty(studentEntityList)) {
            for (StudentEntity studentEntity : studentEntityList) {
                retListStudent.add(studentEntity.formatRetResult());
            }
        }
        return retListStudent;
    }

    /**
     * 中文、拼音混合搜索
     *
     * @param content the content
     * @return dis max query builder
     */
    private DisMaxQueryBuilder structureQuery(String field, String content) {
        //使用dis_max直接取多个query中，分数最高的那一个query的分数即可
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
        //boost 设置权重
        QueryBuilder ikQuery = QueryBuilders.matchQuery(field, content)
                //.analyzer("pinyin_analyzer")
                .boost(2f);
        disMaxQueryBuilder.add(ikQuery);
        return disMaxQueryBuilder;
    }

    /**
     * 拼接搜索条件
     * <p>高亮查询内容, query的值查询两个字段name, address。
     * 当然了你可以配置查询更多个字段或者你可以改成你所需查询的字段</p>
     *
     * @param queryFiled     检索字段
     * @param content       分析内容
     * @return list
     */
    @Override
    public List<Map> searchHighlight(String queryFiled, String content) {
        if(StringUtils.isEmpty(queryFiled)){
            queryFiled = "name";
        }
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮显示规则
        highlightBuilder.preTags("<span style='color:green'>");
        highlightBuilder.postTags("</span>");
        //指定高亮字段
        highlightBuilder.field(queryFiled);
        //String[] fields = {"name", "address"};
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(
                queryFiled, content);
        //QueryBuilder matchQuery = QueryBuilders.multiMatchQuery(
        //        matchQueryBuilder, fields);
        //搜索数据
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(5);

        List<Map> list = new ArrayList<>(16);
        SearchResponse searchResponse = esUtils.search(sourceBuilder);
        SearchHits searchHits = searchResponse.getHits();
        log.info("记录数-->" + searchHits.getTotalHits());
        for (SearchHit hit : searchHits) {
            StudentEntity entity = new StudentEntity();
            Map<String, Object> entityMap = hit.getSourceAsMap();
            log.info(hit.getHighlightFields().toString());
            //高亮字段
            if (!StringUtils.isEmpty(hit.getHighlightFields().get(queryFiled))) {
                Text[] text = hit.getHighlightFields().get(queryFiled).getFragments();
                if("name".equals(queryFiled)){
                    entity.setName(text[0].toString());
                }
                if("address".equals(queryFiled)){
                    entity.setAddress(text[0].toString());
                }
                if("detail_info".equals(queryFiled)){
                    entity.setDetailInfo(text[0].toString());
                }
            }
            //map to object
            if (!CollectionUtils.isEmpty(entityMap)) {
                if (!StringUtils.isEmpty(entityMap.get("id"))) {
                    entity.setId(String.valueOf(entityMap.get("id")));
                }
                if (!StringUtils.isEmpty(entityMap.get("name"))) {
                    entity.setName(String.valueOf(entityMap.get("name")));
                }
            }
            list.add(entity.formatRetResult());
        }
    return list;
}

    /**
     * 单条增加或更新
     * @param stuEntity
     */
    @Override
    public void insertOrUpdateOne(StudentEntity stuEntity) {
        esUtils.insertOrUpdateOne(stuEntity);
    }

    /**
     * 调用 ES 获取 IK 分词后结果
     *
     * @param analyzerContent 待分析内容
     * @param analyzeType 分词类型 ik_max_word(粗分) ik_smart(细分)
     * @return analyze result
     */
    @Override
    public List<AnalyzeResponse.AnalyzeToken> getAnalyzeResult(String analyzerContent,
                                                               String analyzeType) {
        //指定字段分析
        AnalyzeResponse response = esUtils.getAnalysisResponse(
                "detail_info",analyzerContent, analyzeType);
        if(null == response){
            return null;
        }
        return response.getTokens();
    }
}