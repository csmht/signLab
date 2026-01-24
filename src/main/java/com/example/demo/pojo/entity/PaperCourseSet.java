package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "paper_course_set", comment = "试卷课程关联表 - 存储试卷与课程的绑定关系")
@TableIndex(name = "uk_paper_course", fields = {"paperId", "courseId"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_paper_id", fields = {"paperId"})
@TableIndex(name = "idx_course_id", fields = {"courseId"})
public class PaperCourseSet {

    @TableId(type = IdType.AUTO)
    @Column(comment = "关联ID")
    private Long id;

    @Column(comment = "试卷ID", type = "bigint", notNull = true)
    private Long paperId;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "是否是问卷：0-试卷，1-问卷", type = "bit", defaultValue = "0")
    private Boolean isQuestionnaire;

    @Column(comment = "开始时间", type = "datetime")
    private LocalDateTime startTime;

    @Column(comment = "结束时间", type = "datetime")
    private LocalDateTime endTime;

    @Column(comment = "是否允许补做：false-不允许，true-允许", type = "bit", defaultValue = "0")
    private Boolean allowRetake;
}