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
@Table(value = "papers", comment = "试卷表 - 存储试卷的基础信息")
@TableName("papers")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "试卷名称", type = "varchar(255)")
    private String name;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(comment = "创建者用户名", type = "varchar(50)")
    private String createdBy;

    @Column(comment = "是否为问卷：0-试卷，1-问卷", type = "bit")
    private Boolean isQuestionnaire;

    @Column(comment = "是否删除", type = "bit", defaultValue = "0")
    private Boolean isDeleted;
}