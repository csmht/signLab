package com.example.demo.pojo.response;

import com.example.demo.pojo.entity.ProcedureTopic;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师历史小测响应（新版）
 */
@Data
public class ClassroomQuizHistoryResponseV2 {

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

    @Data
    public static class ProcedureTopicInfo {
        private Boolean isRandom;
        private Integer number;
        private String tags;
        private String topicTypes;
        private Boolean tagMatchAll;
        private Integer selectedTopicCount;
        private List<TopicInfo> topics;

        @Data
        public static class TopicInfo {
            private Long topicId;
            private Integer number;
            private Integer type;
            private String content;
            private String choices;
            private String correctAnswer;
        }

        public static ProcedureTopicInfo fromEntity(ProcedureTopic entity, Integer selectedTopicCount) {
            if (entity == null) {
                return null;
            }
            ProcedureTopicInfo info = new ProcedureTopicInfo();
            info.setIsRandom(entity.getIsRandom());
            info.setNumber(entity.getNumber());
            info.setTags(entity.getTags());
            info.setTopicTypes(entity.getTopicTypes());
            info.setTagMatchAll(entity.getTagMatchAll());
            info.setSelectedTopicCount(selectedTopicCount);
            return info;
        }
    }
}
