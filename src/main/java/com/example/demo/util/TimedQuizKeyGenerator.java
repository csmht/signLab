package com.example.demo.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 限时答题密钥生成和验证工具类
 * 密钥格式：username|timestamp|random_code
 * 使用 CryptoUtil 进行加密，不存储到数据库
 */
@Slf4j
@Component
public class TimedQuizKeyGenerator {

    @Autowired
    private CryptoUtil cryptoUtil;

    /** 密钥缓冲时间（毫秒）- 默认5分钟 */
    private static final long KEY_BUFFER_TIME = 5 * 60 * 1000;

    /**
     * 生成密钥
     *
     * @param studentUsername 学生用户名
     * @return 加密后的密钥字符串
     */
    public String generateKey(String studentUsername) {
        try {
            // 1. 生成时间戳
            long timestamp = System.currentTimeMillis();

            // 2. 生成随机验证码（16位）
            String randomCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

            // 3. 构建原始数据
            String rawData = String.format("%s|%d|%s", studentUsername, timestamp, randomCode);

            // 4. 使用CryptoUtil加密
            String encryptedKey = cryptoUtil.encrypt(rawData);

            log.info("为用户 {} 生成密钥，时间戳：{}", studentUsername, timestamp);

            return encryptedKey;

        } catch (Exception e) {
            log.error("生成密钥失败", e);
            throw new RuntimeException("生成密钥失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证密钥
     *
     * @param secretKey    加密的密钥
     * @param studentUsername 学生用户名
     * @param quizTimeLimit 答题时间限制（分钟）
     * @return 验证结果对象
     */
    public KeyValidationResult validateKey(String secretKey, String studentUsername, Integer quizTimeLimit) {
        try {
            // 1. 解密密钥
            String decryptedData = cryptoUtil.decrypt(secretKey);

            // 2. 分割数据
            String[] parts = decryptedData.split("\\|");
            if (parts.length != 3) {
                return new KeyValidationResult(false, "密钥格式错误", null, 0L);
            }

            String keyUsername = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            String randomCode = parts[2];

            // 3. 验证用户名
            if (!keyUsername.equals(studentUsername)) {
                return new KeyValidationResult(false, "用户名不匹配", null, 0L);
            }

            // 4. 验证时间
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - timestamp;
            long timeLimitMillis = quizTimeLimit * 60 * 1000L;

            if (elapsedTime > timeLimitMillis + KEY_BUFFER_TIME) {
                return new KeyValidationResult(false, "答题时间已超时", null, 0L);
            }

            // 5. 计算剩余时间
            long remainingTime = Math.max(0, timeLimitMillis - elapsedTime);

            LocalDateTime keyGeneratedTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
            );

            log.info("密钥验证成功，用户：{}，已用时：{}ms，剩余：{}ms",
                studentUsername, elapsedTime, remainingTime);

            return new KeyValidationResult(true, "验证通过", keyGeneratedTime, remainingTime);

        } catch (Exception e) {
            log.error("验证密钥失败", e);
            return new KeyValidationResult(false, "密钥验证失败: " + e.getMessage(), null, 0L);
        }
    }

    /**
     * 密钥验证结果
     */
    @Data
    public static class KeyValidationResult {
        private boolean valid;
        private String message;
        private LocalDateTime keyGeneratedTime;
        private long remainingTime;  // 毫秒

        public KeyValidationResult(boolean valid, String message,
                                   LocalDateTime keyGeneratedTime, long remainingTime) {
            this.valid = valid;
            this.message = message;
            this.keyGeneratedTime = keyGeneratedTime;
            this.remainingTime = remainingTime;
        }
    }
}
