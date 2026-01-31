package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂小测实体
 * 记录教师在课堂中发起的临时小测活动
 */
@Data
@AutoTable
@Table(value = "classroom_quiz", comment = "课堂小测表")
@TableName("classroom_quiz")
public class ClassroomQuiz {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级实验ID */
    @Column(comment = "班级实验ID", type = "bigint(20)", notNull = true)
    private Long classExperimentId;

    /** 题库配置ID */
    @Column(comment = "题库配置ID", type = "bigint(20)", notNull = true)
    private Long procedureTopicId;

    /** 小测标题 */
    @Column(comment = "小测标题", type = "varchar(255)", notNull = true)
    private String quizTitle;

    /** 小测描述 */
    @Column(comment = "小测描述", type = "text")
    private String quizDescription;

    /** 答题时间限制（分钟） */
    @Column(comment = "答题时间限制（分钟）", type = "int(11)")
    private Integer quizTimeLimit;

    /** 状态:0-未开始,1-进行中,2-已结束 */
    @Column(comment = "状态:0-未开始,1-进行中,2-已结束", type = "tinyint(1)", notNull = true, defaultValue = "0")
    private Integer status;

    /** 开始时间 */
    @Column(comment = "开始时间", type = "datetime")
    private LocalDateTime startTime;

    /** 结束时间 */
    @Column(comment = "结束时间", type = "datetime")
    private LocalDateTime endTime;

    /** 创建者(教师用户名) */
    @Column(comment = "创建者", type = "varchar(50)", notNull = true)
    private String createdBy;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", notNull = true, defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
