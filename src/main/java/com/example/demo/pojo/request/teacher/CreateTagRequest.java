package com.example.demo.pojo.request.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建标签请求
 */
@Data
public class CreateTagRequest {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    private String tagName;

    /**
     * 标签类型：1-学科标签、2-难度标签、3-题型标签、4-自定义标签
     */
    @NotBlank(message = "标签类型不能为空")
    private String type;

    /**
     * 标签描述
     */
    private String description;
}
