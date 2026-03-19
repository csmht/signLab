package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.teacher.CreateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTimedQuizProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateTimedQuizProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertTimedQuizProcedureRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.TeacherProcedureDetailResponse;
import com.example.demo.service.TeacherProcedureCreationService;
import com.example.demo.service.TeacherProcedureQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师创建实验步骤控制器
 * 提供教师创建不同类型实验步骤的接口
 */
@RequestMapping("/api/teacher/procedures")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherProcedureCreationController {

    private final TeacherProcedureCreationService teacherProcedureCreationService;
    private final TeacherProcedureQueryService teacherProcedureQueryService;

    /**
     * 创建视频观看步骤
     * 步骤类型：type=1（观看视频）
     *
     * @param request 创建视频步骤请求
     * @return 步骤ID
     */
    @PostMapping("/video")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createVideoProcedure(@RequestBody CreateVideoProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.createVideoProcedure(request);
        return ApiResponse.success(procedureId, "创建成功");
    }

    /**
     * 创建数据收集步骤
     * 步骤类型：type=2（数据收集）
     *
     * @param request 创建数据收集步骤请求
     * @return 步骤ID
     */
    @PostMapping("/data-collection")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createDataCollectionProcedure(@RequestBody CreateDataCollectionProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.createDataCollectionProcedure(request);
        return ApiResponse.success(procedureId, "创建成功");
    }

    /**
     * 创建题库练习步骤
     * 步骤类型：type=3（题库答题）
     *
     * @param request 创建题库练习步骤请求
     * @return 步骤ID
     */
    @PostMapping("/topic")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createTopicProcedure(@RequestBody CreateTopicProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.createTopicProcedure(request);
        return ApiResponse.success(procedureId, "创建成功");
    }

    /**
     * 修改视频观看步骤
     * 步骤类型：type=1（观看视频）
     *
     * @param request 修改视频步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/video")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateVideoProcedure(@RequestBody UpdateVideoProcedureRequest request) {
        teacherProcedureCreationService.updateVideoProcedure(request);
        return ApiResponse.success(null, "修改成功");
    }

    /**
     * 修改数据收集步骤
     * 步骤类型：type=2（数据收集）
     *
     * @param request 修改数据收集步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/data-collection")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateDataCollectionProcedure(@RequestBody UpdateDataCollectionProcedureRequest request) {
        teacherProcedureCreationService.updateDataCollectionProcedure(request);
        return ApiResponse.success(null, "修改成功");
    }

    /**
     * 修改题库练习步骤
     * 步骤类型：type=3（题库答题）
     *
     * @param request 修改题库练习步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/topic")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateTopicProcedure(@RequestBody UpdateTopicProcedureRequest request) {
        teacherProcedureCreationService.updateTopicProcedure(request);
        return ApiResponse.success(null, "修改成功");
    }

    /**
     * 插入视频观看步骤
     * 步骤类型：type=1（观看视频）
     *
     * @param request 插入视频步骤请求
     * @return 步骤ID
     */
    @PostMapping("/video/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertVideoProcedure(@RequestBody InsertVideoProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.insertVideoProcedure(request);
        return ApiResponse.success(procedureId, "插入成功");
    }

    /**
     * 插入数据收集步骤
     * 步骤类型：type=2（数据收集）
     *
     * @param request 插入数据收集步骤请求
     * @return 步骤ID
     */
    @PostMapping("/data-collection/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertDataCollectionProcedure(@RequestBody InsertDataCollectionProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.insertDataCollectionProcedure(request);
        return ApiResponse.success(procedureId, "插入成功");
    }

    /**
     * 插入题库练习步骤
     * 步骤类型：type=3（题库答题）
     *
     * @param request 插入题库练习步骤请求
     * @return 步骤ID
     */
    @PostMapping("/topic/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertTopicProcedure(@RequestBody InsertTopicProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.insertTopicProcedure(request);
        return ApiResponse.success(procedureId, "插入成功");
    }

    /**
     * 创建限时答题步骤
     * 步骤类型：type=5（限时答题）
     *
     * @param request 创建限时答题步骤请求
     * @return 步骤ID
     */
    @PostMapping("/timed-quiz")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createTimedQuizProcedure(@RequestBody CreateTimedQuizProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.createTimedQuizProcedure(request);
        return ApiResponse.success(procedureId, "创建成功");
    }

    /**
     * 修改限时答题步骤
     * 步骤类型：type=5（限时答题）
     *
     * @param request 修改限时答题步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/timed-quiz")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateTimedQuizProcedure(@RequestBody UpdateTimedQuizProcedureRequest request) {
        teacherProcedureCreationService.updateTimedQuizProcedure(request);
        return ApiResponse.success(null, "修改成功");
    }

    /**
     * 插入限时答题步骤
     * 步骤类型：type=5（限时答题）
     *
     * @param request 插入限时答题步骤请求
     * @return 步骤ID
     */
    @PostMapping("/timed-quiz/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertTimedQuizProcedure(@RequestBody InsertTimedQuizProcedureRequest request) {
        Long procedureId = teacherProcedureCreationService.insertTimedQuizProcedure(request);
        return ApiResponse.success(procedureId, "插入成功");
    }

    /**
     * 删除指定步骤
     *
     * @param procedureId 步骤ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteProcedure(@PathVariable("procedureId") Long procedureId) {
        teacherProcedureCreationService.deleteProcedure(procedureId);
        return ApiResponse.success(null, "删除成功");
    }

    /**
     * 查询单个步骤详情
     *
     * @param procedureId       步骤ID
     * @return 步骤详情
     */
    @GetMapping("/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TeacherProcedureDetailResponse> getProcedureDetail(
            @PathVariable("procedureId") Long procedureId) {
        TeacherProcedureDetailResponse response = teacherProcedureQueryService.getProcedureDetail(procedureId);
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * 查询实验的所有步骤详情
     *
     * @param experimentId      实验ID
     * @return 步骤详情列表
     */
    @GetMapping("/experiment/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<TeacherProcedureDetailResponse>> getExperimentProcedures(
            @PathVariable("experimentId") Long experimentId) {
        List<TeacherProcedureDetailResponse> responses = teacherProcedureQueryService.getExperimentProcedures(experimentId);
        return ApiResponse.success(responses, "查询成功");
    }
}
