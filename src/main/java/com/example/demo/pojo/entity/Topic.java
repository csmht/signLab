package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.exception.BusinessException;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目表
 * 存储试卷中的题目信息
 */
@Data
@AutoTable
@Table(value = "topics", comment = "题目表 - 存储试卷中的题目信息")
@TableName("topics")
@TableIndex(name = "idx_paper_id", fields = {"paperId"})
public class Topic {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷ID */
    @Column(comment = "试卷ID", type = "bigint")
    private Long paperId;

    /** 题号 */
    @Column(comment = "题号", type = "int")
    private Integer number;

    /** 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，6-其他 */
    @Column(comment = "题目类型：1-单选题，2-多选题，3-判断题，4-填空题，6-其他", type = "int")
    private Integer type;

    /** 题目内容 */
    @Column(comment = "题目内容", type = "varchar(255)")
    private String content;

    /**
     * 选项内容
     * 规定：选择题：每个选项之间用 $ 隔开，每个选项的格式为 "选项字母:选项内容"
     * 例如："A:这是a选项$B:这是b选项$C:这是c选项$D:这是d选项"
     */
    @Column(comment = "选项内容", type = "varchar(255)")
    private String choices;

    /**
     * 正确答案
     * 规定：
     * 1. 对于单选题、判断题，回答内容为选项的字母（如A、B、C、D）
     * 2. 对于多选题，回答内容为选项的字母（如A、B、C、D）的组合（如A-B-C）。
     * 3. 对于填空题：
     *    - 单个空：直接填写答案
     * 4. 对于简答题，回答内容为学生的文字回答。
     */
    @Column(comment = "正确答案", type = "varchar(255)")
    private String correctAnswer;

    /** 是否删除：0-未删除，1-已删除 */
    @Column(comment = "是否删除：0-未删除，1-已删除", type = "bit", defaultValue = "0")
    private Boolean isDeleted;

    /** 创建者用户名 */
    @Column(comment = "创建者用户名", type = "varchar(50)")
    private String createdBy;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime")
    private java.time.LocalDateTime createdTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime")
    private java.time.LocalDateTime updatedTime;

}
