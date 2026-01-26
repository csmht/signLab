ackage com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 题目带标签响应DTO
 * 返回题目完整信息及其关联的标签
 */
@Data
public class TopicWithTagsResponse {

    /**
     * 题目ID
     */
    private Long id;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 题号
     */
    private Integer number;

    /**
     * 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-其他
     */
    private Integer type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 选项内容
     */
    private String choices;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Boolean isDeleted;

    /**
     * 关联的标签列表
     */
    private List<TagInfo> tags;

    /**
     * 标签信息内部类
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
         * 标签描述
         */
        private String description;
    }

}