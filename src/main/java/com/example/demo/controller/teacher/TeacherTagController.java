package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.entity.Tag;
import com.example.demo.pojo.request.teacher.CreateTagRequest;
import com.example.demo.pojo.request.teacher.UpdateTagRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端标签管理控制器
 * 提供标签的增删改查接口
 */
@RequestMapping("/api/teacher/tags")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherTagController {

    private final TagService tagService;

    /**
     * 获取所有标签列表
     *
     * @return 标签列表
     */
    @GetMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.list();
        return ApiResponse.success(tags, "查询成功");
    }

    /**
     * 按类型查询标签
     *
     * @param type 标签类型（1-学科，2-难度，3-题型，4-自定义）
     * @return 标签列表
     */
    @GetMapping(params = "type")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<Tag>> getTagsByType(@RequestParam("type") String type) {
        List<Tag> tags = tagService.getTagsByType(type);
        return ApiResponse.success(tags, "查询成功");
    }

    /**
     * 创建标签
     *
     * @param request 创建标签请求
     * @return 标签ID
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createTag(@RequestBody CreateTagRequest request) {
        Long tagId = tagService.createTag(
                request.getTagName(),
                request.getType(),
                request.getDescription()
        );
        return ApiResponse.success(tagId, "创建成功");
    }

    /**
     * 更新标签
     *
     * @param request 更新标签请求
     * @return 是否更新成功
     */
    @PutMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateTag(@RequestBody UpdateTagRequest request) {
        tagService.updateTag(
                request.getTagId(),
                request.getTagName(),
                request.getType(),
                request.getDescription()
        );
        return ApiResponse.success(null, "更新成功");
    }

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{tagId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteTag(@PathVariable("tagId") Long tagId) {
        tagService.deleteTag(tagId);
        return ApiResponse.success(null, "删除成功");
    }
}
