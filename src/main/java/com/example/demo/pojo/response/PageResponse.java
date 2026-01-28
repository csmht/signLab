package com.example.demo.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应
 * 用于返回分页数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 构造分页响应
     */
    public static <T> PageResponse<T> of(Long current, Long size, Long total, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setCurrent(current);
        response.setSize(size);
        response.setTotal(total);
        response.setRecords(records);
        if (size != null && size > 0) {
            response.setPages((total + size - 1) / size);
        } else {
            response.setPages(0L);
        }
        return response;
    }

    public static <T> PageResponse<T> of(Long current, Integer size, Long total, List<T> records) {
        Long sizelong = size.longValue();
        return of(current, sizelong, total, records);
    }
}