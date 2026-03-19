package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.TopicTagMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签题目映射表Mapper
 */
@Mapper
public interface TopicTagMapMapper extends BaseMapper<TopicTagMap> {

    /**
     * 查询包含所有指定标签的题目ID
     *
     * @param tagIds 标签ID列表
     * @param tagCount 标签数量
     * @return 题目ID列表
     */
    @Select("<script>" +
            "SELECT DISTINCT topic_id FROM topic_tag_map " +
            "WHERE topic_id IN (" +
            "    SELECT topic_id FROM topic_tag_map " +
            "    WHERE tag_id IN " +
            "    <foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "        #{tagId}" +
            "    </foreach>" +
            "    GROUP BY topic_id " +
            "    HAVING COUNT(DISTINCT tag_id) = #{tagCount}" +
            ")" +
            "</script>")
    List<Long> selectTopicIdsByAllTags(@Param("tagIds") List<Long> tagIds, @Param("tagCount") int tagCount);

}