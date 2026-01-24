package com.example.demo.pojo.dto;

import lombok.Data;

@Data
public class WeChatStatusDto {

    private Boolean isBound;

    private String wxNickname;

    private String wxAvatar;

    private String bindTime;

    private String wxOpenIdMasked;
}