package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.pojo.entity.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 主题服务
 * 提供主题的业务逻辑处理
 */
@Slf4j
@Service
public class TopicService extends ServiceImpl<TopicMapper, Topic> {

    /**
     * 根据主题代码查询主题
     */
    public Topic getByTopicCode(String topicCode) {
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("topic_code", topicCode);
        return getOne(queryWrapper);
    }
}