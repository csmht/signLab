package com.example.demo.pojo.request.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新标签请求
 */
@Data
public class UpdateTagRequest {

    /**
     * 标���ID
     */
    @NotNull(message = "标签ID不能为空")
    private Long tagId;

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
