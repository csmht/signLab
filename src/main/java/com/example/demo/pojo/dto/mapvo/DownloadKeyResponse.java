package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载密钥响应
 * 用于替代 Map<String, String> 结构（下载密钥）
 */
@Data
public class DownloadKeyResponse {

    /**
     * 下载密钥
     */
    private String downloadKey;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 播放密钥
     */
    private String playKey;

    /**
     * 播放URL
     */
    private String playUrl;

    /**
     * 将 DownloadKeyResponse 转换为 Map<String, String>
     *
     * @param response 下载密钥响应对象
     * @return Map<String, String>
     */
    public static Map<String, String> toMap(DownloadKeyResponse response) {
        if (response == null) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        if (response.getDownloadKey() != null) {
            map.put("downloadKey", response.getDownloadKey());
        }
        if (response.getDownloadUrl() != null) {
            map.put("downloadUrl", response.getDownloadUrl());
        }
        if (response.getPlayKey() != null) {
            map.put("playKey", response.getPlayKey());
        }
        if (response.getPlayUrl() != null) {
            map.put("playUrl", response.getPlayUrl());
        }
        return map;
    }

    /**
     * 从 Map<String, String> 创建 DownloadKeyResponse
     *
     * @param map Map<String, String>
     * @return 下载密钥响应对象
     */
    public static DownloadKeyResponse fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        DownloadKeyResponse response = new DownloadKeyResponse();
        response.setDownloadKey(map.get("downloadKey"));
        response.setDownloadUrl(map.get("downloadUrl"));
        response.setPlayKey(map.get("playKey"));
        response.setPlayUrl(map.get("playUrl"));
        return response;
    }
}
