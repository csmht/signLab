package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "student_class_relations", comment = "学生班级关联表")
@TableName("student_class_relations")
@TableIndex(name = "uk_student_class", fields = {"studentUsername", "classCode"}, type = IndexTypeEnum.UNIQUE)
public class StudentClassRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    @Column(comment = "绑定时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime bindTime;

    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}