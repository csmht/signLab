package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.AnswerFileMapper;
import com.example.demo.pojo.dto.*;
import com.example.demo.pojo.entity.AnswerFile;
import com.example.demo.pojo.entity.User;
import com.example.demo.enums.ResponseCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户认证服务
 * 提供用户登录、密码管理、微信绑定等核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends ServiceImpl<AnswerFileMapper, AnswerFile> {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final WeChatService weChatService;
    private final PasswordUtil passwordUtil;

    /**
     * 用户登录
     * 支持首次登录检测、密码验证、微信绑定等功能
     *
     * @param request 登录请求参数
     * @return 登录响应信息
     * @throws BusinessException 用户不存在或密码错误时抛出
     */
    public LoginResponse login(LoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        if (user.getPasswordSet() == 0) {
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setName(user.getName());
            response.setRole(user.getRole());
            response.setIsFirstLogin(true);
            return response;
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(400, "请输入密码");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException(400, "用户密码未设置，请先设置密码");
        }

        String inputPassword = request.getPassword().trim();
        String storedPassword = user.getPassword();

        if (!passwordUtil.matches(inputPassword, storedPassword)) {
            log.warn("用户 {} 密码验证失败，输入密码长度: {}, 存储密码长度: {}",
                    user.getUsername(), inputPassword.length(), storedPassword.length());
            throw new BusinessException(ResponseCode.PASSWORD_ERROR);
        }

        log.info("用户 {} 密码验证成功", user.getUsername());

        if (request.getWxCode() != null && !request.getWxCode().trim().isEmpty()) {
            try {
                log.info("用户 {} 登录时附带微信code，开始绑定微信", user.getUsername());
                bindWeChatOpenId(user, request.getWxCode());
                log.info("用户 {} 微信绑定成功", user.getUsername());
            } catch (Exception e) {
                log.warn("用户 {} 微信绑定失败，但不影响登录: {}", user.getUsername(), e.getMessage());
            }
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getName());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setRole(user.getRole());
        response.setToken(token);
        response.setIsFirstLogin(false);
        return response;
    }

    /**
     * 绑定微信OpenID
     * 将用户账号与微信账号关联，支持获取微信用户信息
     *
     * @param user 用户对象
     * @param wxCode 微信授权code
     * @throws BusinessException 微信已绑定其他账号或绑定失败时抛出
     */
    private void bindWeChatOpenId(User user, String wxCode) {
        try {
            WeChatAccessTokenDto accessTokenDto = weChatService.getAccessToken(wxCode);
            String openid = accessTokenDto.getOpenid();
            String unionid = accessTokenDto.getUnionid();

            log.info("获取到微信OpenID: {}, UnionID: {}", openid, unionid);

            QueryWrapper<User> openidQuery = new QueryWrapper<>();
            openidQuery.eq("wx_openid", openid);
            openidQuery.ne("id", user.getId());
            Long existCount = userMapper.selectCount(openidQuery);

            if (existCount > 0) {
                throw new BusinessException(400, "该微信已绑定其他账号，一个微信只能绑定一个学号");
            }

            WeChatUserInfoDto userInfo = null;
            try {
                userInfo = weChatService.getUserInfo(accessTokenDto.getAccessToken(), openid);
                log.info("获取到微信用户信息，昵称: {}", userInfo.getNickname());
            } catch (Exception e) {
                log.warn("获取微信用户信息失败，将只绑定openid: {}", e.getMessage());
            }

            user.setWxOpenid(openid);
            user.setWxUnionid(unionid);
            user.setWxBindTime(LocalDateTime.now());

            if (userInfo != null) {
                user.setWxNickname(userInfo.getNickname());
                user.setWxAvatar(userInfo.getHeadimgurl());
            }

            int updateResult = userMapper.updateById(user);

            if (updateResult > 0) {
                log.info("用户 {} 成功绑定微信OpenID: {}", user.getUsername(), openid);
            } else {
                throw new BusinessException(500, "微信绑定失败，数据库更新失败");
            }

        } catch (BusinessException e) {
            log.warn("微信绑定失败，用户：{}，错误：{}", user.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("绑定微信OpenID失败，用户：{}", user.getUsername(), e);
            throw new BusinessException(500, "微信绑定失败: " + e.getMessage());
        }
    }

    /**
     * 用户设置密码
     * 密码必须为6位数字，两次输入必须一致
     * 使用BCrypt加密存储
     *
     * @param request 密码设置请求
     * @throws BusinessException 密码格式错误或用户不存在时抛出
     */
    public void setPassword(SetPasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        request.setUsername(currentUsername);

        if (request.getPassword() == null || !request.getPassword().matches("\\d{6}")) {
            throw new BusinessException(400, "密码必须是6位数字");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        String encryptedPassword = passwordUtil.encode(request.getPassword().trim());
        user.setPassword(encryptedPassword);
        user.setPasswordSet(1);
        int updateResult = userMapper.updateById(user);

        if (updateResult <= 0) {
            throw new BusinessException(500, "密码设置失败");
        }

        log.info("用户 {} 密码设置成功", user.getUsername());
    }
    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名（学号/工号）
     * @return 用户对象，不存在时返回null
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    /**
     * 检查用户是否存在
     *
     * @param username 用户名（学号/工号）
     * @return 用户是否存在
     */
    public boolean checkUserExists(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }
    /**
     * 检查用户状态
     * 返回用户是否存在、是否已设置密码、用户角色等信息
     *
     * @param username 用户名（学号/工号）
     * @return 用户状态信息
     */
    public UserStatusDto checkUserStatus(String username) {
        User user = getUserByUsername(username);

        UserStatusDto status = new UserStatusDto();
        if (user == null) {
            status.setExists(false);
            status.setPasswordSet(false);
        } else {
            status.setExists(true);
            status.setPasswordSet(user.getPasswordSet() == 1);
            status.setRole(user.getRole());
            status.setName(user.getName());
        }

        return status;
    }
    /**
     * 检查用户微信绑定状态
     * 返回用户是否已绑定微信、微信昵称、头像等信息
     *
     * @param username 用户名（学号/工号）
     * @return 微信绑定状态信息
     */
    public WeChatStatusDto checkWeChatStatus(String username) {
        User user = getUserByUsername(username);

        WeChatStatusDto status = new WeChatStatusDto();
        if (user == null) {
            status.setIsBound(false);
            return status;
        }

        boolean isBound = user.getWxOpenid() != null && !user.getWxOpenid().trim().isEmpty();
        status.setIsBound(isBound);

        if (isBound) {
            status.setWxNickname(user.getWxNickname());
            status.setWxAvatar(user.getWxAvatar());

            if (user.getWxBindTime() != null) {
                status.setBindTime(user.getWxBindTime().toString());
            }

            String openId = user.getWxOpenid();
            if (openId != null && openId.length() > 6) {
                String masked = openId.substring(0, 3) + "****" + openId.substring(openId.length() - 3);
                status.setWxOpenIdMasked(masked);
            } else {
                status.setWxOpenIdMasked(openId);
            }
        }

        return status;
    }
    /**
     * 解绑微信
     * 清除用户与微信的绑定关系
     *
     * @param username 用户名（学号/工号）
     * @throws BusinessException 用户不存在或未绑定微信时抛出
     */
    public void unbindWeChat(String username) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        if (user.getWxOpenid() == null || user.getWxOpenid().trim().isEmpty()) {
            throw new BusinessException(400, "该用户未绑定微信");
        }

        user.setWxOpenid(null);
        user.setWxUnionid(null);
        user.setWxNickname(null);
        user.setWxAvatar(null);
        user.setWxBindTime(null);

        int updateResult = userMapper.updateById(user);
        if (updateResult <= 0) {
            throw new BusinessException(500, "微信解绑失败");
        }

        log.info("用户 {} 成功解绑微信", username);
    }
    /**
     * 获取所有已绑定微信的用户
     *
     * @return 已绑定微信的用户列表
     */
    public List<User> getAllUsersWithWeChat() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("wx_openid")
                   .ne("wx_openid", "");
        return userMapper.selectList(queryWrapper);
    }
    /**
     * 调试方法：获取用户详细信息
     * 用于开发调试，返回用户的完整信息字符串
     *
     * @param username 用户名（学号/工号）
     * @return 用户详细信息字符串
     */
    public String debugUserInfo(String username) {
        try {
            log.info("开始查询用户 {}", username);

            if (userMapper == null) {
                return "错误: userMapper为空";
            }

            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);

            log.info("执行数据库查询，用户名 {}", username);
            User user = userMapper.selectOne(queryWrapper);
            log.info("数据库查询完成，结果: {}", user != null ? "找到用户" : "用户不存在");

            if (user == null) {
                return "用户不存在: " + username;
            }

            StringBuilder info = new StringBuilder();
            info.append("用户信息:\n");
            info.append("ID: ").append(user.getId() != null ? user.getId() : "null").append("\n");
            info.append("用户名: ").append(user.getUsername() != null ? user.getUsername() : "null").append("\n");
            info.append("姓名: ").append(user.getName() != null ? user.getName() : "null").append("\n");
            info.append("角色: ").append(user.getRole() != null ? user.getRole() : "null").append("\n");
            info.append("密码设置状态: ").append(user.getPasswordSet() != null ? user.getPasswordSet() : "null").append("\n");
            info.append("密码长度: ").append(user.getPassword() != null ? user.getPassword().length() : "null").append("\n");
            info.append("微信OpenID: ").append(user.getWxOpenid() != null ? user.getWxOpenid() : "未绑定").append("\n");

            log.info("用户信息查询成功");
            return info.toString();
        } catch (Exception e) {
            log.error("调试用户信息失败，用户名: {}", username, e);
            return "查询失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
    /**
     * 测试数据库连接
     * 用于检查数据库连接是否正常
     *
     * @return 数据库连接状态和用户总数
     */
    public String testDatabaseConnection() {
        try {
            log.info("开始测试数据库连接");

            Long userCount = userMapper.selectCount(null);
            log.info("数据库连接正常，用户总数: {}", userCount);

            return "数据库连接正常，用户总数: " + userCount;
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return "数据库连接失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
    /**
     * 通过微信openid登录
     * 适用于已绑定微信的用户快速登录
     *
     * @param openid 微信openid
     * @return 登录响应信息
     * @throws BusinessException 未找到绑定用户或用户被禁用时抛出
     */
    public LoginResponse loginByOpenId(String openid) {
        log.info("开始通过openid登录，openid: {}", openid);

        try {
            User user = getUserByOpenId(openid);
            if (user == null) {
                throw new BusinessException(404, "未找到绑定的用户，请先绑定微信");
            }

            if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
                throw new BusinessException(403, "用户已被禁用");
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getName());

            log.info("openid登录成功，用户: {}", user.getUsername());

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setName(user.getName());
            response.setRole(user.getRole());

            if (user.getWxNickname() != null) {
                response.setWxNickname(user.getWxNickname());
            }
            if (user.getWxAvatar() != null) {
                response.setWxAvatar(user.getWxAvatar());
            }

            return response;

        } catch (BusinessException e) {
            log.warn("openid登录失败，openid: {}，错误: {}", openid, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("openid登录异常，openid: {}", openid, e);
            throw new BusinessException(500, "登录失败: " + e.getMessage());
        }
    }
    /**
     * 通过微信授权code登录
     * 推荐方式：通过微信授权code获取openid后登录
     *
     * @param code 微信授权code
     * @return 登录响应信息
     * @throws BusinessException code无效或登录失败时抛出
     */
    public LoginResponse loginByCode(String code) {
        log.info("开始通过code登录，code: {}", code);

        try {
            String openid = weChatService.getOpenIdByCode(code);
            if (openid == null || openid.trim().isEmpty()) {
                throw new BusinessException(400, "无效的微信code");
            }

            log.info("通过code获取到openid: {}", openid);

            return loginByOpenId(openid);

        } catch (BusinessException e) {
            log.warn("code登录失败，code: {}，错误: {}", code, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("code登录异常，code: {}", code, e);
            throw new BusinessException(500, "登录失败: " + e.getMessage());
        }
    }
    /**
     * 根据openid查找用户
     *
     * @param openid 微信openid
     * @return 用户对象，不存在时返回null
     */
    public User getUserByOpenId(String openid) {
        try {
            log.info("根据openid查找用户，openid: {}", openid);

            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("wx_openid", openid);
            queryWrapper.eq("is_deleted", 0);

            User user = userMapper.selectOne(queryWrapper);

            if (user != null) {
                log.info("找到用户，用户名: {}, 姓名: {}", user.getUsername(), user.getName());
            } else {
                log.warn("未找到绑定的用户，openid: {}", openid);
            }

            return user;

        } catch (Exception e) {
            log.error("根据openid查找用户异常，openid: {}", openid, e);
            return null;
        }
    }
    /**
     * 重置用户密码
     * 仅管理员可调用，密码重置为"syjx@学号后四位"
     * 使用事务确保数据一致性
     *
     * @param usernames 需要重置密码的用户名列表
     * @throws BusinessException 用户不存在或无权限时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(List<String> usernames) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String currentUserRole = null;
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            currentUserRole = authentication.getAuthorities().iterator().next().getAuthority();

            if (currentUserRole.startsWith("ROLE_")) {
                currentUserRole = currentUserRole.substring(5).toLowerCase();
            }

            if("admin".equals(currentUserRole)){

                for(String username : usernames) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<User>().eq("username", username);
                    User user = userMapper.selectOne(queryWrapper);
                    if (user == null) {
                        throw new BusinessException(404, "用户不存在，用户名: " + username + "重置失败");
                    }

                    String lastFourDigits = username.length() >= 4 ?
                            username.substring(username.length() - 4) :
                            username;
                    String password = "syjx@" + lastFourDigits;
                    String encodedPassword = passwordUtil.encode(password);
                    user.setPassword(encodedPassword);
                    user.setPasswordSet(1);
                    userMapper.updateById(user);
                }
            }else{
                throw new BusinessException(404, "无权限重置密码");
            }
        }else{
            throw new BusinessException(404, "无权限重置密码");
        }
    }
}
