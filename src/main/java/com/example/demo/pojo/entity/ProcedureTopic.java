package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import org.checkerframework.checker.units.qual.C;
import org.dromara.autotable.annotation.AutoTable;

import java.time.LocalDateTime;

/**
 * 实验步骤-题库答题表
 * 存储实验学生需要完成的题库答题类型步骤的数据信息
 */
@Data
@AutoTable
@Table(value = "procedure_topic", comment = "实验步骤-题库答题表 - 存储实验学生需要完成的题库答题类型步骤的数据信息")
@TableName("procedure_topic")
public class ProcedureTopic {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 是否随机抽取 */
    @Column(comment = "是否随机抽取" ,type = "bit" , defaultValue = "0" , notNull = true )
    private Boolean isRandom;

    /** 题目数量（仅在随机抽取时有效） */
    @Column(comment = "题目数量（仅在随机抽取时有效）" , type = "int")
    private Integer number;

    /** 标签限制（仅在随机抽取时有效，格式 id1,id2） */
    @Column(comment = "标签限制（仅在随机抽取时有效，格式 id1,id2）" , type = "text")
    private String tags;

    /** 题目类型限制（仅在随机抽取时有效，格式 1,2,3，对应1-单选，2-多选，3-判断，4-填空，6-其他） */
    @Column(comment = "题目类型限制（仅在随机抽取时有效，格式 1,2,3）" , type = "varchar(255)")
    private String topicTypes;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /** 是否删除：0-否，1-是 */
    @Column(comment = "是否删除：0-否，1-是", type = "bit", defaultValue = "0")
    private Boolean isDeleted;

}
