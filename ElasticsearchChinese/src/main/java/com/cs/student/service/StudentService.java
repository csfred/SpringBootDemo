package com.cs.student.service;

import com.cs.student.model.StudentEntity;
import org.elasticsearch.client.indices.AnalyzeResponse;

import java.util.List;
import java.util.Map;

/**
 *
 * @author cs
 * @date 2020/02/17
 */
public interface StudentService {

    /**
     * 拼接搜索条件
     *
     * @param field     查询字段 默认为 name name可以支持拼音
     * @param content   查询内容
     * @return list
     */
    public List<Map> search(String field, String content);

    /**
     * 拼接搜索条件
     * <p>高亮查询内容, query的值查询两个字段name, address。
     * 当然了你可以配置查询更多个字段或者你可以改成你所需查询的字段</p>
     *
     * @param queryFiled     检索字段
     * @param queryCondition 检索条件
     * @return list
     */
    public List<Map> searchHighlight(String queryFiled, String queryCondition);

    /**
     * 单条增加或更新
     */
    public void insertOrUpdateOne(StudentEntity stuEntity);

    /**
     * 调用 ES 获取 IK 分词后结果
     *
     * @param analyzerContent 待分析内容
     * @param analyzeType 分词类型 ik_max_word(粗分) ik_smart(细分)
     * @return analyze result
     */
    public List<AnalyzeResponse.AnalyzeToken> getAnalyzeResult(String analyzerContent,
                                                               String analyzeType);
}