ackage com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;

/**
 * 题库题目标签表
 * 用于对题目进行分类和标记
 */
@Data
@AutoTable
@Table(value = "tags", comment = "题库题目标签")
@TableName("tags")
public class Tag {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    @Column(comment = "标签名称", type = "varchar(50)", notNull = true)
    private String tagName;

    /** 标签描述 */
    @Column(comment = "标签描述", type = "varchar(200)")
    private String description;

}
