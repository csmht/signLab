ackage com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 标签题目映射响应DTO
 * 返回标签及其关联的题目信息
 */
@Data
public class TagTopicMapResponse {

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

    /**
     * 关联的题目数量
     */
    private Integer topicCount;

    /**
     * 关联的题目列表
     */
    private List<TopicInfo> topics;

    /**
     * 题目信息内部类
     */
    @Data
    public static class TopicInfo {
        /**
         * 题目ID
         */
        private Long topicId;

        /**
         * 题号
         */
        private Integer number;

        /**
         * 题目内容
         */
        private String content;

        /**
         * 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-其他
         */
        private Integer type;
    }

}