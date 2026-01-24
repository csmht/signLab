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
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "student_course_paper_topic_answers", comment = "学生试卷题目回答表")
@TableName("student_course_paper_topic_answers")
@TableIndex(name = "idx_paper_course_set_topic", fields = {"paperCourseSetId", "topicId"})
@TableIndex(name = "idx_student_paper_course_set", fields = {"studentUsername", "paperCourseSetId"})
@TableIndex(name = "idx_is_approved", fields = {"isApproved"})
public class StudentCoursePaperTopicAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "试卷课程绑定ID", type = "bigint")
    private Long paperCourseSetId;

    @Column(comment = "题目ID", type = "bigint")
    private Long topicId;

    @Column(comment = "答题学生学号", type = "varchar(255)")
    private String studentUsername;

    @Column(comment = "回答内容", type = "varchar(255)")
    private String answerContent;

    @Column(comment = "是否包含照片：0-不包含，1-包含", type = "bit")
    private Boolean hasPhoto;

    @Column(comment = "是否包含文件：0-不包含，1-包含", type = "bit")
    private Boolean hasFile;

    @Column(comment = "是否批改：0-未批改，1-已批改", type = "bit")
    private Boolean isApproved;

    @Column(comment = "得分", type = "bigint")
    private Long score;

    @Column(comment = "批改用户名", type = "varchar(255)")
    private String approvedByUserName;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}