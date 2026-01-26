ackage com.example.demo.pojo.response;

import lombok.Data;

/**
 * 微信绑定状态DTO
 */
@Data
public class WeChatStatusDto {

    /** 是否已绑定微信 */
    private Boolean isBound;

    /** 微信昵称 */
    private String wxNickname;

    /** 微信头像URL */
    private String wxAvatar;

    /** 绑定时间 */
    private String bindTime;

    /** 微信OpenID（脱敏显示） */
    private String wxOpenIdMasked;
}