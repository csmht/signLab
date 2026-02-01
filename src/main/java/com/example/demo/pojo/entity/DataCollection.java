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
 * 实验步骤-数据收集表
 * 存储实验学生需要完成的数据收集类型步骤的数据信息
 */
@Data
@AutoTable
@Table(value = "data_collection", comment = "实验步骤-数据收集表 - 存储实验学生需要完成的数据收集类型步骤的数据信息")
@TableName("data_collection")
public class DataCollection {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 数据类型（1--关键数据，2--表格数据） */
    @Column(comment = "数据类型（1--关键数据，2--表格数据）" , type = "int", notNull = true)
    private Long type;

    /** 数据描述（JSON格式，存储填空和表格的结构化数据） */
    @Column(comment = "数据描述（JSON格式，存储填空和表格的结��化数据）", type = "text")
    private String remark;

    /** 是否需要提交照片：0-不需要，1-需要 */
    @Column(comment = "是否需要提交照片：0-不需要，1-需要", type = "bit")
    private Boolean needPhoto;

    /** 是否需要提交文档：0-不需要，1-需要 */
    @Column(comment = "是否需要提交文档：0-不需要，1-需要", type = "bit")
    private Boolean needDoc;

    /** 正确答案（JSON格式，用于自动判分） */
    @Column(comment = "正确答案（JSON格式，用于自动判分）", type = "text")
    private String correctAnswer;

    /** 误差范围（可选，用于数值类答案的判分） */
    @Column(comment = "误差范围（可选，用于数值类答案的判分）", type = "double")
    private Double tolerance;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /** 是否删除：0-否，1-是 */
    @Column(comment = "是否删除：0-否，1-是", type = "bit", defaultValue = "0")
    private Boolean isDeleted;
}
