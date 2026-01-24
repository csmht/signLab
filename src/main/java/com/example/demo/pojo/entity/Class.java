package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.*;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "classes", comment = "班级表")
@TableName("classes")
@TableIndex(name = "uk_class_code", fields = {"classCode"},type = IndexTypeEnum.UNIQUE)
public class Class {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    @Column(comment = "班级名称", type = "varchar(100)", notNull = true)
    private String className;

    @Column(comment = "验证码", type = "varchar(10)", notNull = true)
    private String verificationCode;

    @Column(comment = "班级人数", type = "int", defaultValue = "0")
    private Integer studentCount;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}