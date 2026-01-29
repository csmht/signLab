package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.TagMapper;
import com.example.demo.pojo.entity.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签服务
 * 提供标签的业务逻辑处理
 */
@Slf4j
@Service
public class TagService extends ServiceImpl<TagMapper, Tag> {

    @Autowired
    private TopicTagMapService topicTagMapService;

    /**
     * 按类型查询标签
     *
     * @param type 标签类型（1-学科，2-难度，3-题型，4-自定义）
     * @return 标签列表
     */
    public List<Tag> getTagsByType(String type) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.orderByAsc("id");
        return list(wrapper);
    }

    /**
     * 创建标签（需指定type）
     *
     * @param tagName 标签名称
     * @param type 标签类型
     * @param description 标签描述
     * @return 标签ID
     */
    public Long createTag(String tagName, String type, String description) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setType(type);
        tag.setDescription(description);
        save(tag);
        return tag.getId();
    }

    /**
     * 更新标签
     *
     * @param tagId 标签ID
     * @param tagName 标签名称
     * @param type 标签类型
     * @param description 标签描述
     */
    public void updateTag(Long tagId, String tagName, String type, String description) {
        Tag tag = getById(tagId);
        if (tag == null) {
            throw new com.example.demo.exception.BusinessException(404, "标签不存在");
        }

        if (tagName != null) {
            tag.setTagName(tagName);
        }
        if (type != null) {
            tag.setType(type);
        }
        if (description != null) {
            tag.setDescription(description);
        }

        updateById(tag);
    }

    /**
     * 删除标签（同时删除关联）
     *
     * @param tagId 标签ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        // 先删除关联关系
        topicTagMapService.removeByTagId(tagId);

        // 再删除标签
        removeById(tagId);
    }
}
