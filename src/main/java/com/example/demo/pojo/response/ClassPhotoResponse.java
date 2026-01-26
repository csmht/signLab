package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂照片响应
 */
@Data
public class ClassPhotoResponse {

    /**
     * 照片ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 学生用户名
     */
    private String studentUsername;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 照片文件名（原始文件名）
     */
    private String photoName;

    /**
     * 照片访问URL
     */
    private String photoUrl;

    /**
     * 照片备注
     */
    private String remark;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}
