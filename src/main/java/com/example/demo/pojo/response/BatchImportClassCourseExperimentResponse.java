package com.example.demo.pojo.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入班级课程实验响应
 */
@Data
public class BatchImportClassCourseExperimentResponse {

    // ========== 班级统计 ==========
    /** 班级成功数量 */
    private int classSuccessCount = 0;
    /** 班级重复数量 */
    private int classDuplicateCount = 0;
    /** 班级失败数量 */
    private int classFailCount = 0;

    // ========== 课程统计 ==========
    /** 课程成功数量 */
    private int courseSuccessCount = 0;
    /** 课程重复数量 */
    private int courseDuplicateCount = 0;
    /** 课程失败数量 */
    private int courseFailCount = 0;

    // ========== 实验统计 ==========
    /** 实验成功数量 */
    private int experimentSuccessCount = 0;
    /** 实验重复数量 */
    private int experimentDuplicateCount = 0;
    /** 实验失败数量 */
    private int experimentFailCount = 0;

    // ========== 班级实验（课次）统计 ==========
    /** 班级实验成功数量 */
    private int classExperimentSuccessCount = 0;
    /** 班级实验重复数量 */
    private int classExperimentDuplicateCount = 0;
    /** 班级实验失败数量 */
    private int classExperimentFailCount = 0;

    // ========== 错误信息 ==========
    /** 错误信息列表 */
    private List<String> errorMessages = new ArrayList<>();
}
