package com.example.demo.service;

import com.example.demo.config.WeChatConfig;
import com.example.demo.pojo.response.WeChatAccessTokenDto;
import com.example.demo.pojo.response.WeChatUserInfoDto;
import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信服务类
 * 提供微信授权、获取用户信息等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatService {

    private final WeChatConfig weChatConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final com.example.demo.mapper.UserMapper userMapper;

    /**
     * 通过code获取微信access_token和openid
     * 用于微信网页授权登录
     *
     * @param code 微信授权code
     * @return 微信访问令牌信息（包含openid、unionid等）
     * @throws BusinessException code无效或微信API调用失败时抛出
     */
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
                log.warn("微信API返回错误：errcode: {}, errmsg: {}", result.getErrcode(), result.getErrmsg());
                String errorMessage = getWeChatErrorMessage(result.getErrcode(), result.getErrmsg());
                throw new BusinessException(400, errorMessage);
            }

            if (result.getOpenid() == null || result.getOpenid().isEmpty()) {
                throw new BusinessException(500, "获取微信OpenID失败");
            }

            log.info("成功获取微信OpenID: {}", result.getOpenid());
            return result;

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                log.warn("调用微信API异常，业务错误: {}", e.getMessage());
                throw e;
            }
            log.error("调用微信API异常", e);
            throw new BusinessException(500, "获取微信信息失败: " + e.getMessage());
        }
    }
    /**
     * 获取微信用户信息
     * 通过access_token和openid获取用户的昵称、头像等信息
     *
     * @param accessToken 微信访问令牌
     * @param openid 用户唯一标识
     * @return 微信用户信息
     * @throws BusinessException 参数无效或微信API调用失败时抛出
     */
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
                log.warn("微信API返回错误：errcode: {}, errmsg: {}", result.getErrcode(), result.getErrmsg());
                String errorMessage = getWeChatErrorMessage(result.getErrcode(), result.getErrmsg());
                throw new BusinessException(400, "获取微信用户信息失败: " + errorMessage);
            }

            log.info("成功获取微信用户信息，昵称: {}", result.getNickname());
            return result;

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                log.warn("调用微信API异常，业务错误: {}", e.getMessage());
                throw e;
            }
            log.error("调用微信API异常", e);
            throw new BusinessException(500, "获取微信用户信息失败: " + e.getMessage());
        }
    }
    /**
     * 验证微信配置是否正确
     * 检查AppID和AppSecret是否已配置
     *
     * @return 配置是否正确
     */
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
    /**
     * 根据微信错误码返回用户友好的错误信息
     *
     * @param errcode 微信错误码
     * @param errmsg 微信错误信息
     * @return 用户友好的错误信息
     */
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
    /**
     * 解析微信API响应
     * 将JSON字符串解析为DTO对象
     *
     * @param response 微信API返回的JSON字符串
     * @return 解析后的DTO对象
     * @throws BusinessException 解析失败时抛出
     */
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
    /**
     * 通过code获取openid（简化版本）
     * 直接从微信API获取openid，不返回完整的token信息
     *
     * @param code 微信授权code
     * @return 微信openid，失败时返回null
     */
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
            log.warn("通过code获取openid异常，code: {}, 原因: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * 通过微信OpenID查找用户
     *
     * @param wxOpenid 微信OpenID
     * @return 用户信息
     */
    public com.example.demo.pojo.entity.User getUserByWxOpenid(String wxOpenid) {
        if (wxOpenid == null || wxOpenid.trim().isEmpty()) {
            return null;
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.demo.pojo.entity.User> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("wx_openid", wxOpenid).eq("is_deleted", 0);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 绑定微信账号
     *
     * @param username 用户名
     * @param wxOpenid 微信OpenID
     * @param wxUnionid 微信UnionID
     * @param wxNickname 微信昵称
     * @param wxAvatar 微信头像
     * @return 绑定结果
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public com.example.demo.pojo.response.WeChatBindResponse bindWeChat(String username, String wxOpenid, String wxUnionid,
                                                                         String wxNickname, String wxAvatar) {
        // 1. 查询用户
        com.example.demo.pojo.entity.User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.demo.pojo.entity.User>()
                .eq("username", username).eq("is_deleted", 0)
        );
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 检查微信OpenID是否已被其他用户绑定
        if (wxOpenid != null && !wxOpenid.trim().isEmpty()) {
            com.example.demo.pojo.entity.User existingUser = getUserByWxOpenid(wxOpenid);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new BusinessException(400, "该微信账号已被其他用户绑定");
            }
        }

        // 3. 更新用户微信信息
        user.setWxOpenid(wxOpenid);
        user.setWxUnionid(wxUnionid);
        user.setWxNickname(wxNickname);
        user.setWxAvatar(wxAvatar);
        user.setWxBindTime(java.time.LocalDateTime.now());

        boolean updated = userMapper.updateById(user) > 0;
        if (!updated) {
            throw new BusinessException(500, "绑定微信失败");
        }

        log.info("用户 {} 绑定微信成功，OpenID：{}", username, wxOpenid);

        // 4. 构建返回结果
        return buildWeChatBindResponse(user);
    }

    /**
     * 解绑微信账号
     *
     * @param username 用户名
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void unbindWeChat(String username) {
        // 1. 查询用户
        com.example.demo.pojo.entity.User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.demo.pojo.entity.User>()
                .eq("username", username).eq("is_deleted", 0)
        );
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 检查是否已绑定微信
        if (user.getWxOpenid() == null || user.getWxOpenid().trim().isEmpty()) {
            throw new BusinessException(400, "未绑定微信账号");
        }

        // 3. 清除微信信息
        user.setWxOpenid(null);
        user.setWxUnionid(null);
        user.setWxNickname(null);
        user.setWxAvatar(null);
        user.setWxBindTime(null);

        boolean updated = userMapper.updateById(user) > 0;
        if (!updated) {
            throw new BusinessException(500, "解绑微信失败");
        }

        log.info("用户 {} 解绑微信成功", username);
    }

    /**
     * 获取用户微信绑定状态
     *
     * @param username 用户名
     * @return 微信绑定状态
     */
    public com.example.demo.pojo.response.WeChatBindResponse getWeChatBindStatus(String username) {
        // 1. 查询用户
        com.example.demo.pojo.entity.User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.demo.pojo.entity.User>()
                .eq("username", username).eq("is_deleted", 0)
        );
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 构建返回结果
        return buildWeChatBindResponse(user);
    }

    /**
     * 构建微信绑定响应
     */
    private com.example.demo.pojo.response.WeChatBindResponse buildWeChatBindResponse(com.example.demo.pojo.entity.User user) {
        com.example.demo.pojo.response.WeChatBindResponse response = new com.example.demo.pojo.response.WeChatBindResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());

        boolean wxBound = user.getWxOpenid() != null && !user.getWxOpenid().trim().isEmpty();
        response.setWxBound(wxBound);
        response.setWxNickname(user.getWxNickname());
        response.setWxAvatar(user.getWxAvatar());
        response.setWxBindTime(user.getWxBindTime());

        return response;
    }
}