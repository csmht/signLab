package com.example.demo.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 创建班级请求
 */
@Data
public class CreateClassRequest {

    /**
     * 班级编号(可选,不传则自动生成)
     */
    private String classCode;

    /**
     * 班级名称
     */
    @NotBlank(message = "班级名称不能为空")
    private String className;

}