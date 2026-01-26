package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信绑定响应
 */
@Data
public class WeChatBindResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    /**
     * 是否已绑定微信
     */
    private Boolean wxBound;

    /**
     * 微信昵称
     */
    private String wxNickname;

    /**
     * 微信头像
     */
    private String wxAvatar;

    /**
     * 微信绑定时间
     */
    private LocalDateTime wxBindTime;
}
