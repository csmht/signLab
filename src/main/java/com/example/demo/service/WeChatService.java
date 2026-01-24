package com.example.demo.service;

import com.example.demo.config.WeChatConfig;
import com.example.demo.pojo.dto.WeChatAccessTokenDto;
import com.example.demo.pojo.dto.WeChatUserInfoDto;
import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatService {

    private final WeChatConfig weChatConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeChatAccessTokenDto getAccessToken(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(400, "微信授权code不能为空");
        }

        try {
            String url = String.format(
                "%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                weChatConfig.getApi().getOauth2TokenUrl(),
                weChatConfig.getAppid(),
                weChatConfig.getSecret(),
                code
            );

            log.info("正在通过code获取access_token，code: {}", code);

            String response = restTemplate.getForObject(url, String.class);
            log.info("微信API原始响应: {}", response);

            WeChatAccessTokenDto result = parseWeChatResponse(response);

            if (result == null) {
                throw new BusinessException(500, "调用微信API失败：返回结果为空");
            }

            if (result.getErrcode() != null && result.getErrcode() != 0) {
                log.error("微信API返回错误：errcode: {}, errmsg: {}", result.getErrcode(), result.getErrmsg());
                String errorMessage = getWeChatErrorMessage(result.getErrcode(), result.getErrmsg());
                throw new BusinessException(400, errorMessage);
            }

            if (result.getOpenid() == null || result.getOpenid().isEmpty()) {
                throw new BusinessException(500, "获取微信OpenID失败");
            }

            log.info("成功获取微信OpenID: {}", result.getOpenid());
            return result;

        } catch (Exception e) {
            log.error("调用微信API异常", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(500, "获取微信信息失败: " + e.getMessage());
        }
    }

    public WeChatUserInfoDto getUserInfo(String accessToken, String openid) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new BusinessException(400, "access_token不能为空");
        }

        if (openid == null || openid.trim().isEmpty()) {
            throw new BusinessException(400, "openid不能为空");
        }

        try {
            String url = String.format(
                "%s?access_token=%s&openid=%s&lang=zh_CN",
                weChatConfig.getApi().getUserinfoUrl(),
                accessToken,
                openid
            );

            log.info("正在获取微信用户信息，openid: {}", openid);

            WeChatUserInfoDto result = restTemplate.getForObject(url, WeChatUserInfoDto.class);

            if (result == null) {
                throw new BusinessException(500, "调用微信API失败：返回结果为空");
            }

            if (result.getErrcode() != null && result.getErrcode() != 0) {
                log.error("微信API返回错误：errcode: {}, errmsg: {}", result.getErrcode(), result.getErrmsg());
                String errorMessage = getWeChatErrorMessage(result.getErrcode(), result.getErrmsg());
                throw new BusinessException(400, "获取微信用户信息失败: " + errorMessage);
            }

            log.info("成功获取微信用户信息，昵称: {}", result.getNickname());
            return result;

        } catch (Exception e) {
            log.error("调用微信API异常", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(500, "获取微信用户信息失败: " + e.getMessage());
        }
    }

    public boolean validateConfig() {
        if (weChatConfig.getAppid() == null || weChatConfig.getAppid().isEmpty()) {
            log.warn("微信AppID未配置");
            return false;
        }

        if (weChatConfig.getSecret() == null || weChatConfig.getSecret().isEmpty()) {
            log.warn("微信AppSecret未配置");
            return false;
        }

        return true;
    }

    private String getWeChatErrorMessage(Integer errcode, String errmsg) {
        if (errcode == null) {
            return "微信接口调用失败";
        }

        switch (errcode) {
            case 40013:
                return "微信AppID无效，请检查配置";
            case 40001:
                return "微信AppSecret无效，请检查配置";
            case 40125:
                return "微信授权code无效或已过期，请重新获取授权";
            case 40029:
                return "微信授权code无效，请重新获取授权";
            case 40163:
                return "微信授权code已使用过，请重新获取授权";
            case 40164:
                return "微信授权code已过期，请重新获取授权";
            case 42001:
                return "微信access_token已过期，请重新授权";
            case 40014:
                return "微信access_token无效，请重新授权";
            case 43004:
                return "微信用户未关注公众号，无法获取用户信息";
            case 48001:
                return "微信API功能未授权，请检查公众号权限";
            case 40003:
                return "微信用户未授权，请重新授权";
            case 40117:
                return "微信授权失败，请重新授权";
            case 40118:
                return "微信授权失败，请重新授权";
            case 40119:
                return "微信授权失败，请重新授权";
            case 40120:
                return "微信授权失败，请重新授权";
            case 40121:
                return "微信授权失败，请重新授权";
            case 40122:
                return "微信授权失败，请重新授权";
            case 40123:
                return "微信授权失败，请重新授权";
            case 40124:
                return "微信授权失败，请重新授权";
            default:
                if (errmsg != null && !errmsg.isEmpty()) {
                    return "微信授权失败: " + errmsg;
                } else {
                    return "微信授权失败，错误码: " + errcode;
                }
        }
    }

    private WeChatAccessTokenDto parseWeChatResponse(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                throw new BusinessException(500, "微信API返回空响应");
            }

            return objectMapper.readValue(response, WeChatAccessTokenDto.class);
        } catch (Exception e) {
            log.error("解析微信API响应失败，响应内容: {}", response, e);
            throw new BusinessException(500, "解析微信API响应失败: " + e.getMessage());
        }
    }

    public String getOpenIdByCode(String code) {
        try {
            log.info("通过code获取openid，code: {}", code);

            WeChatAccessTokenDto tokenDto = getAccessToken(code);

            if (tokenDto != null && tokenDto.getOpenid() != null) {
                String openid = tokenDto.getOpenid();
                log.info("成功获取openid: {}", openid);
                return openid;
            } else {
                log.warn("获取openid失败，tokenDto: {}", tokenDto);
                return null;
            }

        } catch (Exception e) {
            log.error("通过code获取openid异常，code: {}", code, e);
            return null;
        }
    }
}