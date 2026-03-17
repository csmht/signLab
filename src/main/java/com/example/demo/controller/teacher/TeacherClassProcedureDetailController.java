package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.ClassProcedureDetailRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassStudentProcedureDetailResponse;
import com.example.demo.pojo.response.StudentProcedureDetailWithAnswerResponse;
import com.example.demo.service.ClassStudentProcedureQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师班级学生步骤详情查询控制器
 * 提供查询指定班级中所有学生步骤详情的接口
 */
@RequestMapping("/api/teacher/class-procedure-details")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherClassProcedureDetailController {

    private final ClassStudentProcedureQueryService classStudentProcedureQueryService;

    /**
     * 查询班级学生已提交的视频观看步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生视频观看详情列表
     */
    @GetMapping("/video/completed")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.VideoDetail>> getCompletedVideoDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.VideoDetail> result =
            classStudentProcedureQueryService.getCompletedVideoDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生未提交的视频观看步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生视频观看详情列表
     */
    @GetMapping("/video/uncompleted")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.VideoDetail>> getUncompletedVideoDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.VideoDetail> result =
            classStudentProcedureQueryService.getUncompletedVideoDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生已提交的数据收集步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生数据收集详情列表
     */
    @GetMapping("/data-collection/completed")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.DataCollectionDetail>> getCompletedDataCollectionDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.DataCollectionDetail> result =
            classStudentProcedureQueryService.getCompletedDataCollectionDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生未提交的数据收集步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生数据收集详情列表
     */
    @GetMapping("/data-collection/uncompleted")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.DataCollectionDetail>> getUncompletedDataCollectionDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.DataCollectionDetail> result =
            classStudentProcedureQueryService.getUncompletedDataCollectionDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生已提交的题库答题步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生题库答题详情列表
     */
    @GetMapping("/topic/completed")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TopicDetail>> getCompletedTopicDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TopicDetail> result =
            classStudentProcedureQueryService.getCompletedTopicDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生未提交的题库答题步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生题库答题详情列表
     */
    @GetMapping("/topic/uncompleted")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TopicDetail>> getUncompletedTopicDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TopicDetail> result =
            classStudentProcedureQueryService.getUncompletedTopicDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生已提交的限时答题步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生限时答题详情列表
     */
    @GetMapping("/timed-quiz/completed")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TimedQuizDetail>> getCompletedTimedQuizDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TimedQuizDetail> result =
            classStudentProcedureQueryService.getCompletedTimedQuizDetails(request);

        return ApiResponse.success(result, "查询成功");
    }

    /**
     * 查询班级学生未提交的限时答题步骤详情
     *
     * @param classCode      班级编号
     * @param courseId       课程ID
     * @param experimentId   实验ID
     * @param procedureId    步骤ID
     * @param studentUsername 学生用户名（可选，筛选特定学生）
     * @return 班级学生限时答题详情列表
     */
    @GetMapping("/timed-quiz/uncompleted")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TimedQuizDetail>> getUncompletedTimedQuizDetails(
            @RequestParam("classCode") String classCode,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @RequestParam("procedureId") Long procedureId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        ClassProcedureDetailRequest request = new ClassProcedureDetailRequest();
        request.setClassCode(classCode);
        request.setCourseId(courseId);
        request.setExperimentId(experimentId);
        request.setProcedureId(procedureId);
        request.setStudentUsername(studentUsername);

        ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TimedQuizDetail> result =
            classStudentProcedureQueryService.getUncompletedTimedQuizDetails(request);

        return ApiResponse.success(result, "查询成功");
    }
}
