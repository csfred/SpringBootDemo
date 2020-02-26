package com.cs.student.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 注意路径记得对应好
 *
 * @author cs
 * @date 2020/02/17
 */
@Data
public class StudentEntity {
    private String id;
    private Integer age;
    private String name;
    private String sex;
    private String address;
    private String phone;
    private String detailInfo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Override
    public String toString() {
        return "StudentEntity [id=" + id + ",age="+age+
                ",name="+name+",sex="+sex+",address="+address+
                ",phone="+phone+",detailInfo="+detailInfo+
                ",createTime="+createTime+"]";
    }

    public Map<String, Object> toMapEntry(){
        Map<String,Object> retMapEntry = new HashMap<>(16);
        retMapEntry.put("id", getId());
        retMapEntry.put("name", getName());
        retMapEntry.put("age", getAge());
        retMapEntry.put("sex", getSex());
        retMapEntry.put("phone", getPhone());
        retMapEntry.put("address", getAddress());
        retMapEntry.put("detailInfo", getDetailInfo());
        retMapEntry.put("createTime", getCreateTime());
        return  retMapEntry;
    }

    public Map<String, Object> formatRetResult(){
        Map<String, Object> formatRetMap = new HashMap<>(16);
        formatRetMap.put("身份ID", getId());
        formatRetMap.put("姓名", getName());
        formatRetMap.put("性别", getSex());
        formatRetMap.put("年龄", getAge());
        formatRetMap.put("电话", getPhone());
        formatRetMap.put("住址", getAddress());
        formatRetMap.put("详细信息", getDetailInfo());
        formatRetMap.put("收录时间", getCreateTime());
        return formatRetMap;
    }
}
