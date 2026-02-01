package com.example.demo.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VideoMetadataUtil 单元测试
 * 测试视频时长读取和元数据提取功能
 */
@SpringBootTest
public class VideoMetadataUtilTest {

    @Autowired
    private VideoMetadataUtil videoMetadataUtil;

    /**
     * 测试场景1:文件不存在
     * 预期:返回0,不抛出异常
     */
    @Test
    public void testExtractDuration_FileNotExists() {
        File notExistFile = new File("not-exist.mp4");
        long duration = videoMetadataUtil.extractVideoDuration(notExistFile);

        assertEquals(0L, duration, "不存在的文件应返回0");
    }

    /**
     * 测试场景2:null 参数
     * 预期:返回0,不抛出异常
     */
    @Test
    public void testExtractDuration_NullFile() {
        long duration = videoMetadataUtil.extractVideoDuration((File) null);

        assertEquals(0L, duration, "null文件应返回0");
    }

    /**
     * 测试场景3:路径不是文件(是目录)
     * 预期:返回0,不抛出异常
     */
    @Test
    public void testExtractDuration_DirectoryPath() {
        File directory = new File("src/test/resources");
        long duration = videoMetadataUtil.extractVideoDuration(directory);

        assertEquals(0L, duration, "目录路径应返回0");
    }

    /**
     * 测试场景4:提取完整元数据 - 文件不存在
     * 预期:返回null,不抛出异常
     */
    @Test
    public void testExtractMetadata_FileNotExists() {
        File notExistFile = new File("not-exist.mp4");
        VideoMetadataUtil.VideoMetadata metadata = videoMetadataUtil.extractMetadata(notExistFile);

        assertNull(metadata, "不存在的文件应返回null");
    }

    /**
     * 测试场景5:提取完整元数据 - null 参数
     * 预期:返回null,不抛出异常
     */
    @Test
    public void testExtractMetadata_NullFile() {
        VideoMetadataUtil.VideoMetadata metadata = videoMetadataUtil.extractMetadata(null);

        assertNull(metadata, "null文件应返回null");
    }

    /**
     * 集成测试:使用真实视频文件测试
     * 注意:需要准备测试视频文件到 src/test/resources/videos/ 目录
     *
     * 测试步骤:
     * 1. 准备测试视频文件(正常MP4视频)
     * 2. 调用 extractVideoDuration()
     * 3. 验证返回值大于0
     * 4. 验证 extractMetadata() 返回完整元数据
     */
    @Test
    public void testExtractDuration_RealVideoFile() {
        // 准备测试视频文件
        File testVideo = new File("src/test/resources/videos/test-video.mp4");

        // 如果测试视频不存在,跳过此测试
        if (!testVideo.exists()) {
            System.out.println("测试视频文件不存在,跳过集成测试");
            return;
        }

        // 测试时长提取
        long duration = videoMetadataUtil.extractVideoDuration(testVideo);
        assertTrue(duration > 0, "视频时长应大于0");
        System.out.println("视频时长: " + duration + "秒");

        // 测试元数据提取
        VideoMetadataUtil.VideoMetadata metadata = videoMetadataUtil.extractMetadata(testVideo);
        assertNotNull(metadata, "元数据不应为null");
        assertNotNull(metadata.getDurationSeconds(), "时长不应为null");
        assertNotNull(metadata.getWidth(), "宽度不应为null");
        assertNotNull(metadata.getHeight(), "高度不应为null");

        System.out.println("视频元数据: " + metadata);
    }

    /**
     * 边界测试:损坏的视频文件
     * 预期:返回0,不抛出异常
     */
    @Test
    public void testExtractDuration_CorruptedVideo() {
        // 准备损坏的视频文件(需要手动创建一个非视频文件)
        File corruptedFile = new File("src/test/resources/videos/corrupted.mp4");

        // 如果文件不存在,跳过此测试
        if (!corruptedFile.exists()) {
            System.out.println("损坏视频文件不存在,跳过边界测试");
            return;
        }

        long duration = videoMetadataUtil.extractVideoDuration(corruptedFile);
        assertEquals(0L, duration, "损坏视频应返回0");
    }
}
