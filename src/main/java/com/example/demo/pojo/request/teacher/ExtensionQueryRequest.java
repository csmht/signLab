package com.example.demo.pojo.request.teacher;

import lombok.Data;

/**
 * 步骤延长记录查询请求
 */
@Data
public class ExtensionQueryRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 是否分页（true-分页，false-返回全部）
     */
    private Boolean pageable = true;

    /**
     * 学生用户名（模糊查询）
     */
    private String studentUsername;

    /**
     * 教师用户名（模糊查询）
     */
    private String teacherUsername;

    /**
     * 实验步骤ID
     */
    private Long experimentalProcedureId;
}
