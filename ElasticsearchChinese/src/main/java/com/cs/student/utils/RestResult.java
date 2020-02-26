package com.cs.student.utils;


import org.elasticsearch.client.indices.AnalyzeResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询结果处理
 *
 * @author cs
 * @date 2020/02/17
 */
public class RestResult {

    private final Integer SUCESS_CODE = 100;
    private final Integer ERROR_CODE = 500;

    private static RestResult apiResult;

    private String msg;
    private Integer code;
    private Object object;


    private String toJson() throws Exception {
        Map<String,Object> map = new HashMap<>(8);
        map.put("code", code);
        map.put("msg",msg);
        map.put("data",object);
        return JsonUtils.beanToJson(map);
    }

    private RestResult(String message, Integer inCode){
        msg = message;
        code = inCode;
        object = null;
    }

    public static RestResult prepare(){
        apiResult =  new RestResult("",0);
        return apiResult;
    }


    public String success(List<Map> retList) throws Exception {
        apiResult.code = SUCESS_CODE;
        apiResult.msg = "查询成功";
        apiResult.object = retList;
        return toJson();
    }

    public String success(String message) throws Exception {
        apiResult.code = SUCESS_CODE;
        apiResult.msg = message;
        return toJson();
    }

    public String successAnalyze(List<AnalyzeResponse.AnalyzeToken> analyzeTokenList) throws Exception {
        apiResult.code = SUCESS_CODE;
        apiResult.msg = "操作成功";
        apiResult.object = analyzeTokenList;
        return toJson();
    }

    public String error(String message) throws Exception {
        apiResult.code = ERROR_CODE;
        apiResult.msg = message;
        return toJson();
    }
}
