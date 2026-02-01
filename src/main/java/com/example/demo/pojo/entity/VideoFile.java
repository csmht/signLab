package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

/**
 * 视频文件表
 * 存储学生提交的视频文件信息
 */
@Data
@AutoTable
@Table(value = "video_files", comment = "视频文件表")
@TableName("video_files")
@TableIndex(name = "idx_answer_id", fields = {"answerId"})
public class VideoFile {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 回答ID */
    @Column(comment = "回答ID", type = "bigint")
    private Long answerId;

    /** 原始文件名 */
    @Column(comment = "原始文件名", type = "varchar(255)")
    private String originalFileName;

    /** 视频标题 */
    @Column(comment = "视频标题", type = "varchar(255)")
    private String title;

    @Column(comment = "文件描述" , type = "text")
    private String description;

    /** 存储文件名 */
    @Column(comment = "存储文件名", type = "varchar(255)")
    private String storedFileName;

    /** 视频时长（秒） */
    @Column(comment = "视频时长（秒）",type = "bigint")
    private Long videoSeconds;

    /** 文件存储路径 */
    @Column(comment = "文件存储路径", type = "varchar(255)")
    private String filePath;

    /** 文件大小（字节） */
    @Column(comment = "文件大小（字节）", type = "bigint")
    private Long fileSize;
}
