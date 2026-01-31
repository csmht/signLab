package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课堂小测答案实体
 * 记录学生对课堂小测的答题情况
 */
@Data
@AutoTable
@Table(value = "classroom_quiz_answer", comment = "课堂小测答案表")
@TableName("classroom_quiz_answer")
public class ClassroomQuizAnswer {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课堂小测ID */
    @Column(comment = "课堂小测ID", type = "bigint(20)", notNull = true)
    private Long classroomQuizId;

    /** 学生用户名 */
    @Column(comment = "学生用户名", type = "varchar(50)", notNull = true)
    private String studentUsername;

    /** 班级编号 */
    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    /** 答案内容(JSON格式) */
    @Column(comment = "答案内容", type = "text")
    private String answer;

    /** 得分 */
    @Column(comment = "得分", type = "decimal(5,2)", defaultValue = "0.00")
    private BigDecimal score;

    /** 是否全部正确:0-否,1-是 */
    @Column(comment = "是否全部正确", type = "tinyint(1)", defaultValue = "0")
    private Boolean isCorrect;

    /** 提交时间 */
    @Column(comment = "提交时间", type = "datetime", notNull = true, defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime submissionTime;
}
