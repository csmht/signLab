package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

/**
 * 标签题目映射表
 * 用于建立标签与题目之间的多对多关系
 */
@Data
@AutoTable
@Table(value = "topic_tag_map", comment = "标签题目映射表 - 建立标签与题目之间的多对多关系")
@TableName("topic_tag_map")
@TableIndex(name = "uk_topic_tag", fields = {"topicId", "tagId"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_topic_id", fields = {"topicId"})
@TableIndex(name = "idx_tag_id", fields = {"tagId"})
public class TopicTagMap {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目ID */
    @Column(comment = "题目ID", type = "bigint", notNull = true)
    private Long topicId;

    /** 标签ID */
    @Column(comment = "标签ID", type = "bigint", notNull = true)
    private Long tagId;

}
