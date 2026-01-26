ackage com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.*;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级表
 * 存储班级的基本信息
 */
@Data
@AutoTable
@Table(value = "classes", comment = "班级表")
@TableName("classes")
@TableIndex(name = "uk_class_code", fields = {"classCode"},type = IndexTypeEnum.UNIQUE)
public class Class {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级编号 */
    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    /** 班级名称 */
    @Column(comment = "班级名称", type = "varchar(100)", notNull = true)
    private String className;

    /** 班级人数 */
    @Column(comment = "班级人数", type = "int", defaultValue = "0")
    private Integer studentCount;

    /** 创建者 */
    @Column(comment = "创建者", type = "varchar(50)")
    private String creator;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}