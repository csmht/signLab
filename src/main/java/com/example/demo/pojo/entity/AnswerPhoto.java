package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "answer_photos", comment = "回答图片表")
@TableName("answer_photos")
@TableIndex(name = "idx_answer_id", fields = {"answerId"})
public class AnswerPhoto {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("answer_id")
    @Column(comment = "回答ID", type = "bigint")
    private Long answerId;

    @Column(comment = "照片文件名", type = "varchar(255)")
    private String photoName;

    @Column(comment = "照片存储路径（只保存压缩图）", type = "varchar(255)")
    private String photoPath;

    @Column(comment = "照片备注", type = "varchar(255)")
    private String remark;

    @Column(comment = "照片大小（字节）", type = "bigint")
    private Long fileSize;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @TableLogic
    @Column(comment = "是否删除：0-未删除，1-已删除", type = "int")
    private Integer isDeleted;
}