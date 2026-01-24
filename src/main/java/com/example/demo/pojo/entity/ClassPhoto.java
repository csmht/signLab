package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;

import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "class_photos", comment = "课堂照片表 - 存储学生拍摄的课堂照片，包括原图和压缩图路径")
@TableName("class_photos")
@TableIndex(name = "idx_photos_course_id", fields = {"courseId"})
@TableIndex(name = "idx_photos_student_username", fields = {"studentUsername"})
@TableIndex(name = "idx_class_photos_upload_time", fields = {"uploadTime"})
@TableIndex(name = "idx_class_photos_course_student", fields = {"courseId", "studentUsername"})
public class ClassPhoto {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    @Column(comment = "照片文件名", type = "varchar(200)", notNull = true)
    private String photoName;

    @Column(comment = "照片存储路径", type = "varchar(500)", notNull = true)
    private String photoPath;

    @Column(comment = "压缩后的照片存储路径", type = "varchar(500)")
    private String compressedPhotoPath;

    @Column(comment = "照片备注信息", type = "text")
    private String remark;

    @Column(comment = "照片文件大小，单位：字节", type = "bigint")
    private Long fileSize;

    @Column(comment = "照片上传时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime uploadTime;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}