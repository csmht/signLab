package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * 视频元数据提取工具类
 * 使用 JavaCV(FFmpeg) 读取视频时长、分辨率等信息
 */
@Slf4j
@Component
public class VideoMetadataUtil {

    /** 视频元数据读取超时时间(毫秒) */
    private static final long READ_TIMEOUT_MS = 30000L;

    /** 最小视频时长(秒),小于此值认为视频无效 */
    private static final long MIN_VIDEO_DURATION = 1L;

    /**
     * 提取视频时长(秒)
     *
     * @param videoFilePath 视频文件的绝对路径
     * @return 视频时长(秒),读取失败返回 0
     */
    public long extractVideoDuration(String videoFilePath) {
        return extractVideoDuration(new File(videoFilePath));
    }

    /**
     * 提取视频时长(秒)
     *
     * @param videoFile 视频文件对象
     * @return 视频时长(秒),读取失败返回 0
     */
    public long extractVideoDuration(File videoFile) {
        // 1. 基础校验
        if (videoFile == null || !videoFile.exists()) {
            log.warn("视频文件不存在: {}", videoFile);
            return 0L;
        }

        if (!videoFile.isFile()) {
            log.warn("路径不是文件: {}", videoFile.getAbsolutePath());
            return 0L;
        }

        FFmpegFrameGrabber grabber = null;
        try {
            // 2. 创建 FFmpeg 抓取器
            grabber = new FFmpegFrameGrabber(videoFile);

            // 3. 设置超时(防止大文件长时间阻塞)
            grabber.setOption("timeout", String.valueOf(READ_TIMEOUT_MS * 1000));

            // 4. 启动抓取器
            long startTime = System.currentTimeMillis();
            grabber.start();

            // 5. 获取视频时长(毫秒 -> 秒)
            long durationMs = grabber.getLengthInTime();
            long durationSeconds = durationMs / 1000;

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("成功读取视频时长: 文件={}, 时长={}秒, 耗时={}ms",
                    videoFile.getName(), durationSeconds, elapsedTime);

            // 6. 验证时长有效性
            if (durationSeconds < MIN_VIDEO_DURATION) {
                log.warn("视频时长过短(小于{}秒),可能文件损坏: {}", MIN_VIDEO_DURATION, videoFile.getName());
                return 0L;
            }

            return durationSeconds;

        } catch (Exception e) {
            log.error("读取视频时长失败: {}, 错误: {}", videoFile.getName(), e.getMessage(), e);
            return 0L;

        } finally {
            // 7. 确保资源释放
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (IOException e) {
                    log.warn("释放 FFmpeg 资源失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 提取完整视频元数据(扩展方法)
     *
     * @param videoFile 视频文件对象
     * @return 视频元数据对象,读取失败返回 null
     */
    public VideoMetadata extractMetadata(File videoFile) {
        if (videoFile == null || !videoFile.exists()) {
            log.warn("视频文件不存在: {}", videoFile);
            return null;
        }

        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(videoFile);
            grabber.start();

            VideoMetadata metadata = new VideoMetadata();
            metadata.setDurationSeconds(grabber.getLengthInTime() / 1000);
            metadata.setWidth(grabber.getImageWidth());
            metadata.setHeight(grabber.getImageHeight());
            metadata.setVideoCodec(String.valueOf(grabber.getVideoCodec()));
            metadata.setAudioCodec(String.valueOf(grabber.getAudioCodec()));
            metadata.setFrameRate(grabber.getVideoFrameRate());
            metadata.setBitRate((long) grabber.getVideoBitrate());

            log.info("成功读取视频元数据: {}", metadata);
            return metadata;

        } catch (Exception e) {
            log.error("读取视频元数据失败: {}, 错误: {}", videoFile.getName(), e.getMessage(), e);
            return null;

        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (IOException e) {
                    log.warn("释放 FFmpeg 资源失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 视频元数据实体类
     */
    public static class VideoMetadata {
        private Long durationSeconds;   // 时长(秒)
        private Integer width;          // 视频宽度
        private Integer height;         // 视频高度
        private String videoCodec;      // 视频编码
        private String audioCodec;      // 音频编码
        private Double frameRate;       // 帧率
        private Long bitRate;           // 比特率

        // Getters and Setters
        public Long getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(Long durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public String getVideoCodec() {
            return videoCodec;
        }

        public void setVideoCodec(String videoCodec) {
            this.videoCodec = videoCodec;
        }

        public String getAudioCodec() {
            return audioCodec;
        }

        public void setAudioCodec(String audioCodec) {
            this.audioCodec = audioCodec;
        }

        public Double getFrameRate() {
            return frameRate;
        }

        public void setFrameRate(Double frameRate) {
            this.frameRate = frameRate;
        }

        public Long getBitRate() {
            return bitRate;
        }

        public void setBitRate(Long bitRate) {
            this.bitRate = bitRate;
        }

        @Override
        public String toString() {
            return String.format("VideoMetadata{时长=%d秒, 分辨率=%dx%d, 编码=%s/%s, 帧率=%.2f}",
                    durationSeconds, width, height, videoCodec, audioCodec, frameRate);
        }
    }
}
