package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 主题管理控制器
 * 提供主题的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/topic")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TopicController {

    private final TopicService topicService;

    /**
     * 根据ID查询主题
     * @param id 主题ID
     * @return 主题信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Topic> getById(@PathVariable Long id) {
        try {
            Topic topic = topicService.getById(id);
            if (topic == null) {
                return ApiResponse.error(404, "主题不存在");
            }
            return ApiResponse.success(topic);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据主题代码查询主题
     * @param topicCode 主题代码
     * @return 主题信息
     */
    @GetMapping("/code/{topicCode}")
    public ApiResponse<Topic> getByTopicCode(@PathVariable String topicCode) {
        try {
            Topic topic = topicService.getByTopicCode(topicCode);
            if (topic == null) {
                return ApiResponse.error(404, "主题不存在");
            }
            return ApiResponse.success(topic);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建主题
     * 仅管理员可调用
     * @param topic 主题信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<Topic> create(@RequestBody Topic topic) {
        try {
            boolean success = topicService.save(topic);
            if (success) {
                return ApiResponse.success(topic, "主题创建成功");
            } else {
                return ApiResponse.error(500, "主题创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新主题信息
     * 仅管理员可调用
     * @param id 主题ID
     * @param topic 主题信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Topic topic) {
        try {
            topic.setId(id);
            boolean success = topicService.updateById(topic);
            if (success) {
                return ApiResponse.success(null, "主题更新成功");
            } else {
                return ApiResponse.error(404, "主题不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除主题
     * 仅管理员可调用
     * @param id 主题ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = topicService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "主题删除成功");
            } else {
                return ApiResponse.error(404, "主题不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}