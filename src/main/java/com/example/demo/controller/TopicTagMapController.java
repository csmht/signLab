package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.TopicTagMap;
import com.example.demo.service.TopicTagMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签题目映射管理控制器
 * 提供标签与题目映射关系的查询、创建、删除等接口
 */
@RequestMapping("/api/topic-tag-map")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TopicTagMapController {

    private final TopicTagMapService topicTagMapService;

    /**
     * 根据ID查询标签题目映射
     * @param id 映射ID
     * @return 标签题目映射信息
     */
    @GetMapping("/{id}")
    public ApiResponse<TopicTagMap> getById(@PathVariable Long id) {
        try {
            TopicTagMap topicTagMap = topicTagMapService.getById(id);
            if (topicTagMap == null) {
                return ApiResponse.error(404, "标签题目映射不存在");
            }
            return ApiResponse.success(topicTagMap);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据题目ID查询标签映射列表
     * @param topicId 题目ID
     * @return 标签映射列表
     */
    @GetMapping("/topic/{topicId}")
    public ApiResponse<List<TopicTagMap>> getByTopicId(@PathVariable Long topicId) {
        try {
            List<TopicTagMap> topicTagMaps = topicTagMapService.getByTopicId(topicId);
            return ApiResponse.success(topicTagMaps);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据标签ID查询题目映射列表
     * @param tagId 标签ID
     * @return 题目映射列表
     */
    @GetMapping("/tag/{tagId}")
    public ApiResponse<List<TopicTagMap>> getByTagId(@PathVariable Long tagId) {
        try {
            List<TopicTagMap> topicTagMaps = topicTagMapService.getByTagId(tagId);
            return ApiResponse.success(topicTagMaps);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 为题目添加标签
     * 仅管理员可调用
     * @param topicId 题目ID
     * @param tagId 标签ID
     * @return 操作结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping("/add/{topicId}/{tagId}")
    public ApiResponse<Void> addTagToTopic(
            @PathVariable Long topicId,
            @PathVariable Long tagId) {
        try {
            boolean success = topicTagMapService.addTagToTopic(topicId, tagId);
            if (success) {
                return ApiResponse.success(null, "标签添加成功");
            } else {
                return ApiResponse.error(500, "标签添加失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "添加失败: " + e.getMessage());
        }
    }

    /**
     * 为题目批量添加标签
     * 仅管理员可调用
     * @param topicId 题目ID
     * @param tagIds 标签ID列表
     * @return 操作结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping("/add-batch/{topicId}")
    public ApiResponse<Void> addTagsToTopic(
            @PathVariable Long topicId,
            @RequestBody List<Long> tagIds) {
        try {
            boolean success = topicTagMapService.addTagsToTopic(topicId, tagIds);
            if (success) {
                return ApiResponse.success(null, "批量添加标签成功");
            } else {
                return ApiResponse.error(500, "批量添加标签失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "添加失败: " + e.getMessage());
        }
    }

    /**
     * 创建标签题目映射
     * 仅管理员可调用
     * @param topicTagMap 标签题目映射信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<TopicTagMap> create(@RequestBody TopicTagMap topicTagMap) {
        try {
            boolean success = topicTagMapService.save(topicTagMap);
            if (success) {
                return ApiResponse.success(topicTagMap, "标签题目映射创建成功");
            } else {
                return ApiResponse.error(500, "标签题目映射创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 删除标签题目映射
     * 仅管理员可调用
     * @param id 映射ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = topicTagMapService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "标签题目映射删除成功");
            } else {
                return ApiResponse.error(404, "标签题目映射不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 移除题目的指定标签
     * 仅管理员可调用
     * @param topicId 题目ID
     * @param tagId 标签ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/remove/{topicId}/{tagId}")
    public ApiResponse<Void> removeTagFromTopic(
            @PathVariable Long topicId,
            @PathVariable Long tagId) {
        try {
            boolean success = topicTagMapService.removeTagFromTopic(topicId, tagId);
            if (success) {
                return ApiResponse.success(null, "标签移除成功");
            } else {
                return ApiResponse.error(500, "标签移除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 移除题目的所有标签
     * 仅管理员可调用
     * @param topicId 题目ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/remove-all/{topicId}")
    public ApiResponse<Void> removeAllTagsFromTopic(@PathVariable Long topicId) {
        try {
            boolean success = topicTagMapService.removeAllTagsFromTopic(topicId);
            if (success) {
                return ApiResponse.success(null, "所有标签移除成功");
            } else {
                return ApiResponse.error(500, "标签移除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}