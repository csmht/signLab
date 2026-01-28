package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 视频查询请求
 */
@Data
public class VideoQueryRequest {

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 是否分页查询（true：分页，false：列表）
     */
    private Boolean pageable = true;

    /**
     * 原始文件名（模糊查询）
     */
    private String originalFileName;

    /**
     * 回答ID（精确查询）
     */
    private Long answerId;
}
