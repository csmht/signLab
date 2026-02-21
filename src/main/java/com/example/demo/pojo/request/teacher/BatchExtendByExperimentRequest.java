package com.example.demo.pojo.request.teacher;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 按实验ID批量延长步骤时间请求
 */
@Data
public class BatchExtendByExperimentRequest {

    /**
     * 实验ID
     */
    @NotNull(message = "实验ID不能为空")
    private Long experimentId;

    /**
     * 学生用户名列表
     */
    @NotEmpty(message = "学生用户名列表不能为空")
    private List<String> studentUsernames;

    /**
     * 延长时间（分钟）
     */
    @NotNull(message = "延长时间不能为空")
    @Min(value = 0, message = "延长时间不能为负数")
    private Integer extendedMinutes;
}
