package com.cs.student.controller;

import com.cs.student.model.StudentEntity;
import com.cs.student.service.StudentService;
import com.cs.student.utils.RestResult;
import com.google.common.base.Strings;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Es controller
 *
 * @author cs
 * @date 2020/02/17
 */
@Slf4j
@RestController
@Api(value = "SpringBoot集成ElasticSearch查询测试接口", tags = "StudentController")
public class StudentController {

    private StudentService studentService;

    @Autowired
    public void  setEsSearchService(StudentService studentService){
        this.studentService = studentService;
    }

    /**
     * Search controller result.
     *
     * @param field 查询字段 name 可以支持拼音
     * @param content 查询内容
     * @return the controller result
     */
    @GetMapping("/search/{content}")
    @ApiOperation(value = "查询特定内容", notes = "普通查询", response = String.class)
    @ApiImplicitParam(name = "field", value = "查询字段", required = false, dataType = "String")
    public String search(@RequestParam(name="field", required = false)
                                     String field, @PathVariable String content) {
        if(StringUtils.isEmpty(field)) {
            field = "name";
        }
        try {
            return RestResult.prepare().success(studentService.
                    search(field, content));
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "参数异常";
    }

    /**
     * Search highlight controller result.
     *
     * @param content the 内容
     * @param field 查询字段，默认是name name 可以支持拼音
     * @return the controller result
     */
    @GetMapping("/search/highlight/{content}")
    @ApiOperation(value = "查询特定内容", notes = "高亮显示", response = RestResult.class)
    @ApiImplicitParam(name = "field", value = "查询字段", required = false, dataType = "String")
    public String searchHighlight(@PathVariable String content,
                                  @RequestParam(name="field", required = false) String field) {
        if (!Strings.isNullOrEmpty(content)) {
            try {
                return RestResult.prepare().success(studentService.searchHighlight(field, content));
            }catch (Exception e){
                log.error(e.getMessage());
                return "操作异常";
            }
        }
        return "参数异常";
    }

    /**
     * Save controller result.
     *
     * @param studentEntity the student entity
     * @return the controller result
     */
    @PostMapping("save")
    @ApiOperation(value = "添加学生", response = RestResult.class)
    @ApiImplicitParam(name = "studentEntity", value = "学生实体", required = true, dataType = "ElasticsearchEntity")
    public String save(@RequestBody StudentEntity studentEntity) {
        studentService.insertOrUpdateOne(studentEntity);
        try {
            return RestResult.prepare().success("处理成功");
        }catch (Exception e){
            log.error(e.getMessage());
            return "操作异常";
        }
    }

    /**
     * Gets analyze result.
     *
     * @param analyzerContent  the content
     * @param analyzeType the analyze type
     * @return the analyze result
     */
    @GetMapping("/getAnalyzeResult")
    @ApiOperation(value = "测试分词结果", notes = "提供接口用于查看分词结果", response = RestResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "analyzerContent", value = "分词内容", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "analyzeType", value = "分词类型", dataType = "String", paramType = "query")
    })
    public String getAnalyzeResult(@RequestParam(name = "analyzerContent") String analyzerContent,
                                       @RequestParam(name = "analyzeType", required = false) String analyzeType) {
        try {
            return RestResult.prepare().successAnalyze(studentService.
                    getAnalyzeResult(analyzerContent, analyzeType));
        }catch (Exception e){
            log.error(e.getMessage());
            return "操作异常";
        }
    }
}