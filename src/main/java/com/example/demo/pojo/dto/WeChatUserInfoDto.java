package com.example.demo.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信用户信息DTO
 */
@Data
public class WeChatUserInfoDto {

    /** 用户的唯一标识 */
    @JsonProperty("openid")
    private String openid;

    /** 用户昵称 */
    @JsonProperty("nickname")
    private String nickname;

    /** 用户的性别，值为1时是男性，值为2时是女性，值为0时是未知 */
    @JsonProperty("sex")
    private Integer sex;

    /** 用户个人资料填写的省份 */
    @JsonProperty("province")
    private String province;

    /** 普通用户个人资料填写的城市 */
    @JsonProperty("city")
    private String city;

    /** 国家，如中国为CN */
    @JsonProperty("country")
    private String country;

    /** 用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像） */
    @JsonProperty("headimgurl")
    private String headimgurl;

    /** 用户特权信息，json 数组，如微信沃卡用户为（chinaunicom） */
    @JsonProperty("privilege")
    private String[] privilege;

    /** 只有在用户将公众号绑定到微信开放平台账号后，才会出现该字段 */
    @JsonProperty("unionid")
    private String unionid;

    /** 错误码 */
    @JsonProperty("errcode")
    private Integer errcode;

    /** 错误信息 */
    @JsonProperty("errmsg")
    private String errmsg;
}