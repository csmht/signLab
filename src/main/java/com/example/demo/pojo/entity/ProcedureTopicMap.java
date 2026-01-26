ackage com.example.demo.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;

/**
 * 实验步骤-题库答题映射表
 * 存储实验学生需要完成的题库答题类型步骤的数据信息
 */
@Data
@AutoTable
@Table(value = "procedure_topic_topic", comment = "实验步骤-题库答题表 - 存储实验学生需要完成的题库答题类型步骤的数据信息")
@TableName("procedure_topic_topic")
public class ProcedureTopicMap {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 题目ID */
    @Column(comment = "题目ID" , type = "bigint" , notNull = true )
    private Long topicId;

    /** 实验步骤-题库答题表ID */
    @Column(comment = "实验步骤-题库答题表ID", type = "bigint" ,notNull = true )
    private Long procedureTopicId;

}
