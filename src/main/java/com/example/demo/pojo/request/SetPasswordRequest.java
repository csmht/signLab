ackage com.example.demo.pojo.request;

import lombok.Data;

/**
 * 设置密码请求DTO
 */
@Data
public class SetPasswordRequest {

    /** 新密码（必须为6位数字） */
    private String password;

    /** 确认密码 */
    private String confirmPassword;
}