ackage com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回答图片表
 * 存储学生提交的照片文件信息
 */
@Data
@AutoTable
@Table(value = "answer_photos", comment = "回答图片表")
@TableName("answer_photos")
@TableIndex(name = "idx_answer_id", fields = {"answerId"})
public class AnswerPhoto {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 回答ID */
    @Column(comment = "回答ID", type = "bigint")
    private Long answerId;

    /** 照片文件名 */
    @Column(comment = "照片文件名", type = "varchar(255)")
    private String photoName;

    /** 照片存储路径（只保存压缩图） */
    @Column(comment = "照片存储路径（只保存压缩图）", type = "varchar(255)")
    private String photoPath;

    /** 照片备注 */
    @Column(comment = "照片备注", type = "varchar(255)")
    private String remark;

    /** 照片大小（字节） */
    @Column(comment = "照片大小（字节）", type = "bigint")
    private Long fileSize;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /** 是否删除：0-未删除，1-已删除 */
    @TableLogic
    @Column(comment = "是否删除：0-未删除，1-已删除", type = "int")
    private Integer isDeleted;
}