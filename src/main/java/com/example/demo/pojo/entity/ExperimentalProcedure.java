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
 * 实验步骤表
 * 存储实验学生需要完成的步骤信息
 */
@Data
@AutoTable
@Table(value = "experiment_procedure", comment = "实验步骤表 - 存储实验学生需要完成的步骤信息")
@TableName("experiment_procedure")
public class ExperimentalProcedure {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验ID */
    @Column(comment = "实验ID", type = "bigint", notNull = true)
    private Long experimentId;

    /** 步骤序号 */
    @Column(comment = "步骤序号",type = "int")
    private Integer number;

    /** 是否可跳过 */
    @Column(comment = "是否可跳过",type = "bit", notNull = true,defaultValue = "0")
    private Boolean isSkip;

    /** 步骤分数占比 */
    @Column(comment = "步骤分数占比",type = "int",notNull = true,defaultValue = "0")
    private Integer proportion;

    /** 步骤类型（1-观看视频，2-数据收集，3-题库答题） */
    @Column(comment = "步骤类型（1-观看视频，2-数据收集，3-题库答题）")
    private Integer Type;

    /** 步骤描述 */
    @Column(comment = "步骤描述" , type = "text")
    private String remark;

    /** 视频ID(仅类型为1时有效) */
    @Column(comment = "视频ID(仅类型为1时有效)" ,type = "bigint")
    private Long videoId;

    /** 数据收集ID（仅类型为2时有效） */
    @Column(comment = "数据收集ID（仅类型为2时有效）",type = "bigint")
    private Long dataCollectionId;

    /** 题库详情ID(仅类型为3时有效) */
    @Column(comment = "题库详情ID(仅类型为3时有效)",type = "bigint")
    private Long procedureTopicId;

    /** 步骤开始填写时间 */
    @Column(comment = "步骤开始填写时间", type = "datetime", notNull = true)
    private LocalDateTime startTime;

    /** 步骤结束填写时间 */
    @Column(comment = "步骤结束填写时间", type = "datetime", notNull = true)
    private LocalDateTime endTime;

    /** 是否删除 */
    @Column(comment = "是否删除",type = "bit", notNull = true,defaultValue = "0")
    private Boolean isDeleted;

}
