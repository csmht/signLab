package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "experiment_procedure", comment = "实验步骤表 - 存储实验学生需要完成的步骤信息")
@TableName("experiment_procedure")
public class ExperimentalProcedure {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "步骤序号",type = "int")
    private Integer number;

    @Column(comment = "是否可跳过",type = "bit", notNull = true,defaultValue = "0")
    private Boolean isSkip;

    @Column(comment = "步骤分数占比",type = "int",notNull = true,defaultValue = "0")
    private Integer proportion;

    @Column(comment = "步骤类型（1-观看视频，2-数据收集，3-题库答题，4-提交实验报告）")
    private Integer Type;

    @Column(comment = "是否需要提交照片：0-不需要，1-需要", type = "bit")
    private Boolean needPhoto;

    @Column(comment = "是否需要提交文档：0-不需要，1-需要", type = "bit")
    private Boolean needDoc;

    @Column(comment = "是否删除",type = "bit", notNull = true,defaultValue = "0")
    private Boolean isDeleted;

}
