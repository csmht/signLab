ackage com.example.demo.pojo.response;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    /** 用户ID */
    private Long userId;

    /** 用户名（学号/工号） */
    private String username;

    /** 用户姓名 */
    private String name;

    /** 用户角色 */
    private String role;

    /** JWT Token */
    private String token;

    /** 是否首次登录 */
    private Boolean isFirstLogin;

    /** 微信昵称 */
    private String wxNickname;

    /** 微信头像URL */
    private String wxAvatar;
}