package com.example.demo.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WeChatAccessTokenDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("openid")
    private String openid;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("unionid")
    private String unionid;

    @JsonProperty("errcode")
    private Integer errcode;

    @JsonProperty("errmsg")
    private String errmsg;
}