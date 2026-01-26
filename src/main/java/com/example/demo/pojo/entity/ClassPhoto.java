package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂照片表
 * 存储学生上传的课堂照片信息
 */
@Data
@AutoTable
@Table(value = "class_photos", comment = "课堂照片表 - 存储学生上传的课堂照片信息")
@TableName("class_photos")
public class ClassPhoto {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    /** 实验ID */
    @Column(comment = "实验ID", type = "varchar(20)", notNull = true)
    private String experimentId;

    /** 学生用户名(学号) */
    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    /** 照片文件名 */
    @Column(comment = "照片文件名", type = "varchar(200)", notNull = true)
    private String photoName;

    /** 照片存储路径 */
    @Column(comment = "照片存储路径", type = "varchar(500)", notNull = true)
    private String photoPath;

    /** 照片URL（访问地址） */
    @Column(comment = "照片URL（访问地址）", type = "varchar(500)")
    private String photoUrl;

    /** 照片备注 */
    @Column(comment = "照片备注", type = "text")
    private String remark;

    /** 文件大小（字节） */
    @Column(comment = "文件大小（字节）", type = "bigint")
    private Long fileSize;

    /** 上传时间 */
    @Column(comment = "上传时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime uploadTime;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}
