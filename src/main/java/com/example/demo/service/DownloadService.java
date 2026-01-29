package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ProcedureSubmissionMapper;
import com.example.demo.mapper.StudentProcedureAttachmentMapper;
import com.example.demo.mapper.VideoFileMapper;
import com.example.demo.pojo.entity.ProcedureSubmission;
import com.example.demo.pojo.entity.StudentProcedureAttachment;
import com.example.demo.pojo.entity.VideoFile;
import com.example.demo.util.CryptoUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

/**
 * 下载服务
 * 提供文件和视频的密钥生成及下载功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadService {

    private final CryptoUtil cryptoUtil;
    private final VideoFileMapper videoFileMapper;
    private final ProcedureSubmissionMapper procedureSubmissionMapper;
    private final StudentProcedureAttachmentMapper studentProcedureAttachmentMapper;

    @Value("${slz.download.sign-secret:slz_video_sign_secret_2024}")
    private String signSecret;

    @Value("${slz.download.play-expire-minutes:30}")
    private int playExpireMinutes;

    /** 文件存储基础路径 */
    public static final String FILE_BASE_PATH = "uploads" + File.separator + "signlab" + File.separator;

    /** 文件类型：视频 */
    public static final String TYPE_VIDEO = "video";
    /** 文件类型：步骤提交文件 */
    public static final String TYPE_SUBMISSION = "submission";
    /** 文件类型：步骤附件 */
    public static final String TYPE_ATTACHMENT = "attachment";

    /**
     * 生成视频下载密钥
     *
     * @param videoId 视频文件ID
     * @param username 用户名
     * @return 加密的下载密钥
     */
    public String generateVideoKey(Long videoId, String username) {
        VideoFileKey videoFileKey = new VideoFileKey();
        videoFileKey.setType(TYPE_VIDEO);
        videoFileKey.setId(videoId);
        videoFileKey.setKey(UUID.randomUUID().toString());
        videoFileKey.setUsername(username);
        return cryptoUtil.encrypt(videoFileKey.toString());
    }

    /**
     * 生成文件下载密钥
     *
     * @param fileType 文件类型（submission或attachment）
     * @param fileId 文件ID
     * @param username 用户名
     * @return 加密的下载密钥
     */
    public String generateFileKey(String fileType, Long fileId, String username) {
        FileKey fileKey = new FileKey();
        fileKey.setType(fileType);
        fileKey.setId(fileId);
        fileKey.setKey(UUID.randomUUID().toString());
        fileKey.setUsername(username);
        return cryptoUtil.encrypt(fileKey.toString());
    }

    /**
     * 根据密钥下载视频
     *
     * @param encryptedKey 加密的密钥
     * @return 视频文件信息
     */
    public VideoFileDownloadInfo getVideoByKey(String encryptedKey) {
        String decrypted = cryptoUtil.decrypt(encryptedKey);
        VideoFileKey videoFileKey = new VideoFileKey(decrypted);

        if (!TYPE_VIDEO.equals(videoFileKey.getType())) {
            throw new BusinessException(400, "密钥类型错误");
        }

        VideoFile videoFile = videoFileMapper.selectById(videoFileKey.getId());
        if (videoFile == null) {
            throw new BusinessException(404, "视频文件不存在");
        }

        String fullPath = FILE_BASE_PATH + videoFile.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new BusinessException(404, "视频文件不存在");
        }

        VideoFileDownloadInfo downloadInfo = new VideoFileDownloadInfo();
        downloadInfo.setFilePath(fullPath);
        downloadInfo.setFileName(videoFile.getOriginalFileName());
        downloadInfo.setFileSize(videoFile.getFileSize());
        return downloadInfo;
    }

    /**
     * 根据密钥下载文件
     *
     * @param encryptedKey 加密的密钥
     * @return 文件信息
     */
    public FileDownloadInfo getFileByKey(String encryptedKey) {
        String decrypted = cryptoUtil.decrypt(encryptedKey);
        FileKey fileKey = new FileKey(decrypted);

        String fileType = fileKey.getType();
        FileDownloadInfo downloadInfo = new FileDownloadInfo();

        if (TYPE_SUBMISSION.equals(fileType)) {
            ProcedureSubmission submission = procedureSubmissionMapper.selectById(fileKey.getId());
            if (submission == null) {
                throw new BusinessException(404, "文件不存在");
            }
            String fullPath = FILE_BASE_PATH + submission.getFilePath();
            File file = new File(fullPath);
            if (!file.exists()) {
                throw new BusinessException(404, "文件不存在");
            }
            downloadInfo.setFilePath(fullPath);
            downloadInfo.setFileName(submission.getFileName());
            downloadInfo.setFileSize(submission.getFileSize());
        } else if (TYPE_ATTACHMENT.equals(fileType)) {
            StudentProcedureAttachment attachment = studentProcedureAttachmentMapper.selectById(fileKey.getId());
            if (attachment == null) {
                throw new BusinessException(404, "文件不存在");
            }
            String fullPath = FILE_BASE_PATH + attachment.getFilePath();
            File file = new File(fullPath);
            if (!file.exists()) {
                throw new BusinessException(404, "文件不存在");
            }
            downloadInfo.setFilePath(fullPath);
            downloadInfo.setFileName(attachment.getOriginalFileName());
            downloadInfo.setFileSize(attachment.getFileSize());
        } else {
            throw new BusinessException(400, "不支持的文件类型");
        }

        return downloadInfo;
    }

    /**
     * 生成视频播放密钥（带签名和过期时间）
     *
     * @param videoId 视频文件ID
     * @param username 用户名
     * @return 播放密钥
     */
    public String generatePlayKey(Long videoId, String username) {
        try {
            long expireTime = System.currentTimeMillis() + playExpireMinutes * 60 * 1000L;
            String data = videoId + "|" + username + "|" + expireTime;
            String signature = generateHmacSignature(data);
            String playKey = data + "|" + signature;
            return cryptoUtil.encrypt(playKey);
        } catch (Exception e) {
            log.error("生成播放密钥失败", e);
            throw new BusinessException(500, "生成播放密钥失败");
        }
    }

    /**
     * 验证播放密钥并获取视频信息
     *
     * @param encryptedPlayKey 加密的播放密钥
     * @return 视频文件信息
     */
    public VideoFilePlayInfo getVideoByPlayKey(String encryptedPlayKey) {
        try {
            // 解密播放密钥
            String playKey = cryptoUtil.decrypt(encryptedPlayKey);
            String[] parts = playKey.split("\\|");

            if (parts.length != 4) {
                throw new BusinessException(400, "播放密钥格式错误");
            }

            Long videoId = Long.parseLong(parts[0]);
            String username = parts[1];
            long expireTime = Long.parseLong(parts[2]);
            String signature = parts[3];

            // 验证过期时间
            if (System.currentTimeMillis() > expireTime) {
                throw new BusinessException(400, "播放密钥已过期");
            }

            // 验证签名
            String data = videoId + "|" + username + "|" + expireTime;
            String expectedSignature = generateHmacSignature(data);
            if (!expectedSignature.equals(signature)) {
                throw new BusinessException(400, "播放密钥签名无效");
            }

            // 获取视频文件信息
            VideoFile videoFile = videoFileMapper.selectById(videoId);
            if (videoFile == null) {
                throw new BusinessException(404, "视频文件不存在");
            }

            String fullPath = FILE_BASE_PATH + videoFile.getFilePath();
            File file = new File(fullPath);
            if (!file.exists()) {
                throw new BusinessException(404, "视频文件不存在");
            }

            VideoFilePlayInfo playInfo = new VideoFilePlayInfo();
            playInfo.setFilePath(fullPath);
            playInfo.setFileName(videoFile.getOriginalFileName());
            playInfo.setFileSize(videoFile.getFileSize());
            playInfo.setUsername(username);
            return playInfo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证播放密钥失败", e);
            throw new BusinessException(400, "播放密钥无效");
        }
    }

    /**
     * 生成HMAC-SHA256签名
     *
     * @param data 待签名数据
     * @return 签名结果
     */
    private String generateHmacSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                signSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成签名失败", e);
            throw new BusinessException(500, "生成签名失败");
        }
    }

    /**
     * 视频文件密钥对象
     */
    @Data
    private static class VideoFileKey {
        private String type;
        private Long id;
        private String key;
        private String username;

        public VideoFileKey() {
        }

        public VideoFileKey(String key) {
            String[] split = key.split("\\|");
            this.type = split[0];
            this.id = Long.parseLong(split[1]);
            this.key = split[2];
            this.username = split.length > 3 ? split[3] : null;
        }

        public String toString() {
            return type + "|" + id + "|" + key + "|" + username;
        }
    }

    /**
     * 文件密钥对象
     */
    @Data
    private static class FileKey {
        private String type;
        private Long id;
        private String key;
        private String username;

        public FileKey() {
        }

        public FileKey(String key) {
            String[] split = key.split("\\|");
            this.type = split[0];
            this.id = Long.parseLong(split[1]);
            this.key = split[2];
            this.username = split.length > 3 ? split[3] : null;
        }

        public String toString() {
            return type + "|" + id + "|" + key + "|" + username;
        }
    }

    /**
     * 视频文件下载信息
     */
    @Data
    public static class VideoFileDownloadInfo {
        private String filePath;
        private String fileName;
        private Long fileSize;
    }

    /**
     * 视频文件播放信息
     */
    @Data
    public static class VideoFilePlayInfo {
        private String filePath;
        private String fileName;
        private Long fileSize;
        private String username;
    }

    /**
     * 文件下载信息
     */
    @Data
    public static class FileDownloadInfo {
        private String filePath;
        private String fileName;
        private Long fileSize;
    }
}
