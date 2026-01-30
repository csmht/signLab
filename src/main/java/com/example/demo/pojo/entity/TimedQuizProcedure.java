package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

/**
 * 限时答题题目配置表
 * 存储限时答题步骤的题目配置信息
 */
@Data
@AutoTable
@Table(value = "timed_quiz_procedure", comment = "限时答题题目配置表")
@TableName("timed_quiz_procedure")
public class TimedQuizProcedure {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 是否随机抽取题目 */
    @Column(comment = "是否随机抽取", type = "bit", defaultValue = "0")
    private Boolean isRandom;

    /** 题目数量（随机模式有效） */
    @Column(comment = "题目数量", type = "int")
    private Integer topicNumber;

    /** 标签限制（逗号分隔） */
    @Column(comment = "标签限制", type = "varchar(500)")
    private String topicTags;

    /** 题目类型限制（逗号分隔） */
    @Column(comment = "题目类型限制（逗号分隔）", type = "varchar(200)")
    private String topicTypes;

    /** 答题时间限制（分钟） */
    @Column(comment = "答题时间限制（分钟）", type = "int", notNull = true)
    private Integer quizTimeLimit;
}
