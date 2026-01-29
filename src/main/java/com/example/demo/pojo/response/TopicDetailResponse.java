package com.example.demo.pojo.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 题目详情响应（含标签）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TopicDetailResponse extends TopicResponse {

    /**
     * 关联的标签列表
     */
    private List<TagInfo> tags;

    /**
     * 标签信息（含类型）
     */
    @Data
    public static class TagInfo {
        /**
         * 标签ID
         */
        private Long tagId;

        /**
         * 标签名称
         */
        private String tagName;

        /**
         * 标签类型：1-学科标签、2-难度标签、3-题型标签、4-自定义标签
         */
        private String tagType;

        /**
         * 标签描述
         */
        private String description;
    }
}
