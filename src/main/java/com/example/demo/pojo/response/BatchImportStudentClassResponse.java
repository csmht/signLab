package com.example.demo.pojo.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入学生和班级响应类
 * 封装导入结果统计信息
 */
@Data
public class BatchImportStudentClassResponse {

    /**
     * 成功导入学生数
     */
    private int studentSuccessCount = 0;

    /**
     * 重复学生数
     */
    private int studentDuplicateCount = 0;

    /**
     * 失败学生数
     */
    private int studentFailCount = 0;

    /**
     * 成功导入班级数
     */
    private int classSuccessCount = 0;

    /**
     * 重复班级数
     */
    private int classDuplicateCount = 0;

    /**
     * 失败班级数
     */
    private int classFailCount = 0;

    /**
     * 成功绑定数
     */
    private int bindSuccessCount = 0;

    /**
     * 失败绑定数
     */
    private int bindFailCount = 0;

    /**
     * 错误详情列表
     */
    private List<String> errorMessages = new ArrayList<>();
}
