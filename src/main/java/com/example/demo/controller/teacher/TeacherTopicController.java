package com.example.demo.controller.teacher;

import com.alibaba.excel.EasyExcel;
import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.listener.TopicImportListener;
import com.example.demo.pojo.excel.TopicImportExcel;
import com.example.demo.pojo.request.TopicQueryRequest;
import com.example.demo.pojo.request.teacher.BatchDeleteTopicRequest;
import com.example.demo.pojo.request.teacher.CreateTopicRequest;
import com.example.demo.pojo.request.teacher.UpdateTopicRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.response.TopicDetailResponse;
import com.example.demo.pojo.response.TopicStatisticsResponse;
import com.example.demo.service.TagService;
import com.example.demo.service.TopicService;
import com.example.demo.util.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final TagService tagService;

    /**
     * 创建题目
     *
     * @param request 创建题目请求
     * @return 题目ID
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createTopic(@RequestBody CreateTopicRequest request) {
        String teacherUsername = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

        Long topicId = topicService.createTopic(request, teacherUsername);
        return ApiResponse.success(topicId, "创建成功");
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
        topicService.updateTopic(request);
        return ApiResponse.success(null, "更新成功");
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
        String username = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

        topicService.deleteTopic(topicId, username);
        return ApiResponse.success(null, "删除成功");
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
        String username = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

        topicService.batchDeleteTopics(request.getTopicIds(), username);
        return ApiResponse.success(null, "批量删除成功");
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
        PageResponse<TopicDetailResponse> response = topicService.queryTopics(request);
        return ApiResponse.success(response, "查询成功");
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
        TopicDetailResponse response = topicService.getTopicDetail(topicId);
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * 获取题目统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TopicStatisticsResponse> getStatistics() {
        TopicStatisticsResponse response = topicService.getStatistics();
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * Excel批量上传题目
     *
     * @param file Excel文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Map<String, Object>> uploadTopicsByExcel(@RequestParam("file") MultipartFile file) {
        // 校验文件
        if (file.isEmpty()) {
            return ApiResponse.error(400, "上传文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ApiResponse.error(400, "文件格式错误，仅支持Excel文件（.xlsx 或 .xls）");
        }

        // 获取当前用户
        String username = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

        // 创建监听器
        TopicImportListener listener = new TopicImportListener(topicService, tagService, username);

        // 读取Excel文件
        EasyExcel.read(file.getInputStream(), TopicImportExcel.class, listener).sheet().doRead();

        // 获取导入结果
        TopicImportListener.ImportResult result = listener.getResult();

        // 构建返回数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("successCount", result.getSuccessCount());
        responseData.put("failCount", result.getFailCount());
        responseData.put("total", result.getSuccessCount() + result.getFailCount());

        if (result.hasErrors()) {
            responseData.put("errors", result.getErrorMessages());
            return ApiResponse.success(responseData,
                String.format("导入完成，成功%d条，失败%d条", result.getSuccessCount(), result.getFailCount()));
        } else {
            return ApiResponse.success(responseData,
                String.format("导入成功，共导入%d道题目", result.getSuccessCount()));
        }
    }

    /**
     * 下载题目导入模板
     *
     * @param response HTTP响应
     */
    @GetMapping("/template")
    @RequireRole(value = UserRole.TEACHER)
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题目导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<TopicImportExcel> demoData = new java.util.ArrayList<>();

            // 示例1：单选题
            TopicImportExcel topic1 = new TopicImportExcel();
            topic1.setCourseTag("数学");
            topic1.setDifficultyTag("简单");
            topic1.setCustomTag("考题");
            topic1.setTopicType("单选题");
            topic1.setContent("1+1等于多少？");
            topic1.setCorrectAnswer("A");
            topic1.setChoices("{\"A\":\"2\",\"B\":\"3\",\"C\":\"4\",\"D\":\"5\"}");
            demoData.add(topic1);

            // 示例2：多选题
            TopicImportExcel topic2 = new TopicImportExcel();
            topic2.setCourseTag("语文");
            topic2.setDifficultyTag("中等");
            topic2.setCustomTag("考题");
            topic2.setTopicType("多选题");
            topic2.setContent("以下哪些是水果？");
            topic2.setCorrectAnswer("A-B-C");
            topic2.setChoices("{\"A\":\"苹果\",\"B\":\"香蕉\",\"C\":\"橙子\",\"D\":\"白菜\"}");
            demoData.add(topic2);

            { // 示例3：判断题
            TopicImportExcel topic3 = new TopicImportExcel();
            topic3.setCourseTag("英语");
            topic3.setDifficultyTag("困难");
            topic3.setCustomTag("考题");
            topic3.setTopicType("判断题");
            topic3.setContent("地球是圆的。");
            topic3.setCorrectAnswer("正确");
            topic3.setChoices(null);
            demoData.add(topic3);
            }

            { // 示例3：判断题
                TopicImportExcel topic3 = new TopicImportExcel();
                topic3.setCourseTag("地理");
                topic3.setDifficultyTag("困难");
                topic3.setCustomTag("考题");
                topic3.setTopicType("判断题");
                topic3.setContent("地球是方的。");
                topic3.setCorrectAnswer("错误");
                topic3.setChoices(null);
                demoData.add(topic3);
            }

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), TopicImportExcel.class)
                    .sheet("题目导入")
                    .doWrite(demoData);

            log.info("题目导入模板下载成功");
        } catch (Exception e) {
            log.error("下载题目导入模板失败", e);
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }
}
