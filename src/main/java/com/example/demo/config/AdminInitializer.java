package com.example.demo.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.User;
import com.example.demo.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 管理员账号初始化器
 * 应用启动时自动检查并创建默认管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer{

    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_NAME = "系统管理员";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_ROLE = "admin";

    public void start() {
        try {
            // 检查管理员账号是否存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", ADMIN_USERNAME);
            User existingAdmin = userMapper.selectOne(queryWrapper);

            if (existingAdmin == null) {
                // 创建默认管理员账号
                User admin = new User();
                admin.setUsername(ADMIN_USERNAME);
                admin.setName(ADMIN_NAME);
                admin.setPassword(passwordUtil.encode(ADMIN_PASSWORD));
                admin.setRole(ADMIN_ROLE);
                admin.setPasswordSet(1);
                admin.setIsDeleted(0);

                int result = userMapper.insert(admin);

                if (result > 0) {
                    log.info("========================================");
                    log.info("默认管理员账号创建成功！");
                    log.info("用户名: {}", ADMIN_USERNAME);
                    log.info("密码: {}", ADMIN_PASSWORD);
                    log.info("========================================");
                    log.warn("请尽快修改默认管理员密码以确保系统安全！");
                } else {
                    log.error("创建默认管理员账号失败");
                }
            } else {
                log.info("管理员账号已存在，用户名: {}", ADMIN_USERNAME);
            }
        } catch (Exception e) {
            log.error("初始化管理员账号时发生错误", e);
        }
    }
}