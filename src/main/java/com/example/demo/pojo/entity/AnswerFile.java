package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

@Data
@AutoTable
@Table(value = "answer_files", comment = "回答文件表")
@TableName("answer_files")
@TableIndex(name = "idx_answer_id", fields = {"answerId"})
public class AnswerFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "回答ID", type = "bigint")
    private Long answerId;

    @Column(comment = "原始文件名", type = "varchar(255)")
    private String originalFileName;

    @Column(comment = "存储文件名", type = "varchar(255)")
    private String storedFileName;

    @Column(comment = "文件存储路径", type = "varchar(255)")
    private String filePath;

    @Column(comment = "文件大小（字节）", type = "bigint")
    private Long fileSize;
}