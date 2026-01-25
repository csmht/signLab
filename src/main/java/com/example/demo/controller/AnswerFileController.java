package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.AnswerFile;
import com.example.demo.service.AnswerFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 答题文件管理控制器
 * 提供答题文件的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/answer-file")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnswerFileController {

    private final AnswerFileService answerFileService;

    /**
     * 根据ID查询答题文件
     * @param id 答题文件ID
     * @return 答题文件信息
     */
    @GetMapping("/{id}")
    public ApiResponse<AnswerFile> getById(@PathVariable Long id) {
        try {
            AnswerFile answerFile = answerFileService.getById(id);
            if (answerFile == null) {
                return ApiResponse.error(404, "答题文件不存在");
            }
            return ApiResponse.success(answerFile);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生用户名查询答题文件
     * @param studentUsername 学生用户名
     * @return 答题文件信息
     */
    @GetMapping("/student/{studentUsername}")
    public ApiResponse<AnswerFile> getByStudentUsername(@PathVariable String studentUsername) {
        try {
            AnswerFile answerFile = answerFileService.getByStudentUsername(studentUsername);
            if (answerFile == null) {
                return ApiResponse.error(404, "答题文件不存在");
            }
            return ApiResponse.success(answerFile);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建答题文件
     * 学生和教师可调用
     * @param answerFile 答题文件信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<AnswerFile> create(@RequestBody AnswerFile answerFile) {
        try {
            boolean success = answerFileService.save(answerFile);
            if (success) {
                return ApiResponse.success(answerFile, "答题文件创建成功");
            } else {
                return ApiResponse.error(500, "答题文件创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新答题文件信息
     * 学生和教师可调用
     * @param id 答题文件ID
     * @param answerFile 答题文件信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AnswerFile answerFile) {
        try {
            answerFile.setId(id);
            boolean success = answerFileService.updateById(answerFile);
            if (success) {
                return ApiResponse.success(null, "答题文件更新成功");
            } else {
                return ApiResponse.error(404, "答题文件不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除答题文件
     * 仅管理员可调用
     * @param id 答题文件ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = answerFileService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "答题文件删除成功");
            } else {
                return ApiResponse.error(404, "答题文件不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}