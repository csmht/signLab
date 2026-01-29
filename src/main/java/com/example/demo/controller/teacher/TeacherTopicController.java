package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.TopicQueryRequest;
import com.example.demo.pojo.request.teacher.BatchDeleteTopicRequest;
import com.example.demo.pojo.request.teacher.CreateTopicRequest;
import com.example.demo.pojo.request.teacher.UpdateTopicRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.response.TopicDetailResponse;
import com.example.demo.pojo.response.TopicStatisticsResponse;
import com.example.demo.service.TopicService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端题目管理控制器
 * 提供题目的增删改查接口
 */
@RequestMapping("/api/teacher/topics")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherTopicController {

    private final TopicService topicService;

    /**
     * 创建题目
     *
     * @param request 创建题目请求
     * @return 题目ID
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createTopic(@RequestBody CreateTopicRequest request) {
        try {
            String teacherUsername = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            Long topicId = topicService.createTopic(request, teacherUsername);
            return ApiResponse.success(topicId, "创建成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建题目失败", e);
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新题目
     *
     * @param request 更新题目请求
     * @return 是否更新成功
     */
    @PutMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateTopic(@RequestBody UpdateTopicRequest request) {
        try {
            topicService.updateTopic(request);
            return ApiResponse.success(null, "更新��功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新题目失败", e);
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除题目（软删除）
     *
     * @param topicId 题目ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{topicId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteTopic(@PathVariable("topicId") Long topicId) {
        try {
            String username = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            topicService.deleteTopic(topicId, username);
            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除题目失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除题目
     *
     * @param request 批量删除请求
     * @return 是否批量删除成功
     */
    @DeleteMapping("/batch")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> batchDeleteTopics(@RequestBody BatchDeleteTopicRequest request) {
        try {
            String username = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            topicService.batchDeleteTopics(request.getTopicIds(), username);
            return ApiResponse.success(null, "批量删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量删除题目失败", e);
            return ApiResponse.error(500, "批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询题目列表（分页，支持多条件筛选）
     *
     * @param request 查询请求
     * @return 题目列表
     */
    @PostMapping("/query")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<PageResponse<TopicDetailResponse>> queryTopics(@RequestBody TopicQueryRequest request) {
        try {
            PageResponse<TopicDetailResponse> response = topicService.queryTopics(request);
            return ApiResponse.success(response, "查询成功");
        } catch (Exception e) {
            log.error("查询题目列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询题目详情
     *
     * @param topicId 题目ID
     * @return 题目详情
     */
    @GetMapping("/{topicId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TopicDetailResponse> getTopicDetail(@PathVariable("topicId") Long topicId) {
        try {
            TopicDetailResponse response = topicService.getTopicDetail(topicId);
            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询题目详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取题目统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TopicStatisticsResponse> getStatistics() {
        try {
            TopicStatisticsResponse response = topicService.getStatistics();
            return ApiResponse.success(response, "查询成功");
        } catch (Exception e) {
            log.error("获取题目统计失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
