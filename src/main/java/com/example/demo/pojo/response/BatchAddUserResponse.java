package com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 批量添加用户响应DTO
 */
@Data
public class BatchAddUserResponse {

    /**
     * 成功添加的用户数量
     */
    private Integer successCount;

    /**
     * 重复的用户数量
     */
    private Integer duplicateCount;

    /**
     * 失败的用户数量
     */
    private Integer failCount;

    /**
     * 失败的用户列表
     */
    private List<FailedUser> failedUsers;

    @Data
    public static class FailedUser {
        /**
         * 用户名
         */
        private String username;

        /**
         * 姓名
         */
        private String name;

        /**
         * 失败原因
         */
        private String reason;
    }
}