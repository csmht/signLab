package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.remark.FillBlankRemarkDTO;
import com.example.demo.pojo.dto.remark.TableRemarkDTO;
import lombok.Data;

/**
 * 数据收集详情基础响应
 */
@Data
public class BaseDataCollectionDetailResponse {
    /**
     * 数据收集ID
     */
    private Long id;

    /**
     * 数据收集类型（1-关键数据，2-表格数据，3-文件）
     */
    private Integer type;

    /**
     * 填空类型数据描述（type=1时返回）
     */
    private FillBlankRemarkDTO fillBlankRemark;

    /**
     * 表格类型数据描述（type=2时返回）
     */
    private TableRemarkDTO tableRemark;

    /**
     * 是否需要提交照片
     */
    private Boolean needPhoto;

    /**
     * 是否需要提交文档
     */
    private Boolean needDoc;
}
