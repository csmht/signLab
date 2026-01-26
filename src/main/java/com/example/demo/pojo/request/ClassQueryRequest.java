ackage com.example.demo.pojo.request;

import lombok.Data;

/**
 * 班级查询请求
 * 用于班级的查询、分页查询
 */
@Data
public class ClassQueryRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 班级代码（精确查询）
     */
    private String classCode;

    /**
     * 班级名称（模糊查询）
     */
    private String className;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 是否分页查询（true-分页，false-列表）
     */
    private Boolean pageable = true;
}