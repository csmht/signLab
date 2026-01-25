package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.AnswerPhoto;
import com.example.demo.service.AnswerPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 答题照片管理控制器
 * 提供答题照片的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/answer-photo")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnswerPhotoController {

    private final AnswerPhotoService answerPhotoService;

    /**
     * 根据ID查询答题照片
     * @param id 答题照片ID
     * @return 答题照片信息
     */
    @GetMapping("/{id}")
    public ApiResponse<AnswerPhoto> getById(@PathVariable Long id) {
        try {
            AnswerPhoto answerPhoto = answerPhotoService.getById(id);
            if (answerPhoto == null) {
                return ApiResponse.error(404, "答题照片不存在");
            }
            return ApiResponse.success(answerPhoto);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生用户名查询答题照片
     * @param studentUsername 学生用户名
     * @return 答题照片信息
     */
    @GetMapping("/student/{studentUsername}")
    public ApiResponse<AnswerPhoto> getByStudentUsername(@PathVariable String studentUsername) {
        try {
            AnswerPhoto answerPhoto = answerPhotoService.getByStudentUsername(studentUsername);
            if (answerPhoto == null) {
                return ApiResponse.error(404, "答题照片不存在");
            }
            return ApiResponse.success(answerPhoto);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建答题照片
     * 学生和教师可调用
     * @param answerPhoto 答题照片信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<AnswerPhoto> create(@RequestBody AnswerPhoto answerPhoto) {
        try {
            boolean success = answerPhotoService.save(answerPhoto);
            if (success) {
                return ApiResponse.success(answerPhoto, "答题照片创建成功");
            } else {
                return ApiResponse.error(500, "答题照片创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新答题照片信息
     * 学生和教师可调用
     * @param id 答题照片ID
     * @param answerPhoto 答题照片信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AnswerPhoto answerPhoto) {
        try {
            answerPhoto.setId(id);
            boolean success = answerPhotoService.updateById(answerPhoto);
            if (success) {
                return ApiResponse.success(null, "答题照片更新成功");
            } else {
                return ApiResponse.error(404, "答题照片不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除答题照片
     * 仅管理员可调用
     * @param id 答题照片ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = answerPhotoService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "答题照片删除成功");
            } else {
                return ApiResponse.error(404, "答题照片不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}