package com.example.demo.pojo.response;

import com.example.demo.pojo.entity.ProcedureTopic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师历史小测响应
 */
@Data
public class ClassroomQuizHistoryResponse {

    /** 小测ID */
    private Long id;

    /** 班级实验ID */
    private Long classExperimentId;

    /** 小测标题 */
    private String quizTitle;

    /** 小测描述 */
    private String quizDescription;

    /** 答题时间限制（分钟） */
    private Integer quizTimeLimit;

    /** 状态:0-未开始,1-进行中,2-已结束 */
    private Integer status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 创建者(教师用户名) */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 题库配置信息 */
    private ProcedureTopicInfo procedureTopic;

    /**
     * 题库配置信息（简化版）
     */
    @Data
    public static class ProcedureTopicInfo {
        /** 题库配置ID */
        private Long id;

        /** 是否随机抽取 */
        private Boolean isRandom;

        /** 题目数量（仅在随机抽取时有效） */
        private Integer number;

        /** 标签限制（仅在随机抽取时有效，格式 id1,id2） */
        private String tags;

        /** 题目类型限制（仅在随机抽取时有效） */
        private String topicTypes;

        /** 选定的题目数量（非随机模式下） */
        private Integer selectedTopicCount;

        /**
         * 从 ProcedureTopic 实体创建
         */
        public static ProcedureTopicInfo fromEntity(ProcedureTopic entity, Integer selectedTopicCount) {
            if (entity == null) {
                return null;
            }
            ProcedureTopicInfo info = new ProcedureTopicInfo();
            info.setId(entity.getId());
            info.setIsRandom(entity.getIsRandom());
            info.setNumber(entity.getNumber());
            info.setTags(entity.getTags());
            info.setTopicTypes(entity.getTopicTypes());
            info.setSelectedTopicCount(selectedTopicCount);
            return info;
        }
    }
}
