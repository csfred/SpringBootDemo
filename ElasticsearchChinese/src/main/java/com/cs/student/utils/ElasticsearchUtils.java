package com.cs.student.utils;

import com.alibaba.fastjson.JSON;
import com.cs.student.config.EsClientConfig;
import com.cs.student.model.StudentEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cs
 * @date 2020/02/20
 */
@Slf4j
@Component
public class ElasticsearchUtils {

    private EsClientConfig esClientConfig;

    private RestHighLevelClient client;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public void setEsClientConfiguration(EsClientConfig esClientConfig){
        this.esClientConfig = esClientConfig;
        client = this.esClientConfig.esTemplate();
    }

    /**
     * 断某个index是否存在
     * @param idxName index名
     * @return boolean
     */
    public boolean isExistsIndex(String idxName) throws Exception {
        return client.indices().exists(new GetIndexRequest(StreamInput.wrap(
                idxName.getBytes())),
                RequestOptions.DEFAULT);
    }

    /**
     * 设置分片
     * @param request http请求
     * @return void
     */
    private void buildSetting(CreateIndexRequest request){
        request.settings(Settings.builder().put("index.number_of_shards",3)
                .put("index.number_of_replicas",2));
    }

    /**
     * @param entity  对象
     * @return void
     */
    public void insertOrUpdateOne(StudentEntity entity) {
        String idxName = esClientConfig.getIndexName();
        IndexRequest request = new IndexRequest(idxName);
        request.id(entity.getId());
        try {
            request.source(mapper.writeValueAsString(entity), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量插入数据
     *
     * @param list 带插入列表
     * @return void
     */
    public void insertBatch(List<StudentEntity> list) {
        String idxName = esClientConfig.getIndexName();
        BulkRequest request = new BulkRequest();
        list.forEach(item -> {
            try {
                request.add(
                    new IndexRequest(idxName).id(item.getId())
                            .source(mapper.writeValueAsString(item), XContentType.JSON));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量删除
     *
     * @param idList  待删除列表
     * @return void
     */
    public <T> void deleteBatch(Collection<T> idList) {
        String idxName = esClientConfig.getIndexName();
        BulkRequest request = new BulkRequest();
        try {
            idList.forEach(item -> request.add(
                    new DeleteRequest(idxName, item.toString())));
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检索信息
     *
     * @param builder   查询参数
     * @param c 结果类对象
     * @return java.util.List<T>
     */
    public <T> List<T> search(SearchSourceBuilder builder, Class<T> c) {
        String idxName = esClientConfig.getIndexName();
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                res.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SearchResponse search(SearchSourceBuilder builder) {
        String idxName = esClientConfig.getIndexName();
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        SearchResponse searchResponse = null;
        try {
             searchResponse = client.search(request,
                    RequestOptions.DEFAULT);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return searchResponse;
    }

    /**
     * 删除index
     *
     * @param idxName index 名称
     * @return void
     */
    public void deleteIndex(String idxName) {
        try {
            if (!isExistsIndex(idxName)) {
                log.error(" idxName={} 不存在",idxName);
                return;
            }
            client.indices().delete(new DeleteIndexRequest(idxName),
                    RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建索引
     *
     * @param idxName   索引名称
     * @param idxSql    索引描述
     * @return void
     */
    public void createIndex(String idxName,String idxSql){
        try {
            if (isExistsIndex(idxName)) {
                log.error(" idxName={} 已经存在,idxSql={}",idxName,idxSql);
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(idxName);

            buildSetting(request);

            request.mapping(idxSql, XContentType.JSON);
            //request.settings() 手工指定Setting
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * 根据指定条件删除
     *
     * @param idxName index 名称
     * @param builder 构造器
     * @return void
     */
    public void deleteByQuery(String idxName, QueryBuilder builder) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(idxName);
        request.setQuery(builder);
        //设置批量操作数量,最大为10000
        request.setBatchSize(10000);
        request.setConflicts("proceed");
        try {
            client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AnalyzeResponse getAnalysisResponse(String analyzerField,
                                               String analyzerContent,
                                               String analyzeType){
        //AnalyzeRequest analyzeRequest = AnalyzeRequest.withField("student",
        //        analyzerField,analyzerContent);
        //AnalyzeRequest analyzeRequest = AnalyzeRequest.withNormalizer(
        //        "student", analyzeType, analyzerContent);
        if(StringUtils.isEmpty(analyzeType)){
            analyzeType = "ik_analyzer";
        }
        String idxName = esClientConfig.getIndexName();
        AnalyzeRequest analyzeRequest = AnalyzeRequest.withField(idxName,
                analyzerField,analyzerContent);
        AnalyzeResponse response = null;
        try {
            response = client.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
        }catch (IOException e){
            log.error(e.getMessage());
        }
        return response;
    }
}
