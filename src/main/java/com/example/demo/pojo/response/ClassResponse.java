ackage com.example.demo.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级响应
 * 用于返回班级基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {

    /**
     * 班级ID
     */
    private Long id;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 班级人数
     */
    private Integer studentCount;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}