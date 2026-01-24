package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {

    private String appid;

    private String secret;

    private String templateId;

    private Api api = new Api();

    @Data
    public static class Api {
        private String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";

        private String oauth2TokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";

        private String userinfoUrl = "https://api.weixin.qq.com/sns/userinfo";
    }
}