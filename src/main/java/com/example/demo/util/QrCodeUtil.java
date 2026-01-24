package com.example.demo.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class QrCodeUtil {

    @Value("${wx.qr.domain:}")
    private String domain;

    public String generateQrCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败：" + e.getMessage());
        }
    }

    public String generateAttendanceQrContent(String courseId, String teacherCode, String classCode, Long timestamp) {
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("%s|%s|%s|%d|%s", courseId, teacherCode, classCode, timestamp, randomCode);
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    public String generateMultiClassAttendanceQrContent(String courseId, String teacherCode, Long timestamp) {
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("%s|%s|MULTI|%d|%s", courseId, teacherCode, timestamp, randomCode);
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    public String generateAttendanceQrUrl(String courseId, String teacherCode, String classCode, Long timestamp) {
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("%s|%s|%s|%d|%s", courseId, teacherCode, classCode, timestamp, randomCode);
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());
        return String.format("https://gdutsyjx.gdut.edu.cn/signlab/student/scan?qr=%s", encodedContent);
    }

    public String generateMultiClassAttendanceQrUrl(String courseId, String teacherCode, Long timestamp) {
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("%s|%s|MULTI|%d|%s", courseId, teacherCode, timestamp, randomCode);
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());
        return String.format("https://gdutsyjx.gdut.edu.cn/signlab/student/scan?qr=%s", encodedContent);
    }

    public Map<String, String> parseAttendanceQrContent(String qrContent) {
        try {
            String decodedContent = new String(Base64.getDecoder().decode(qrContent));
            String[] parts = decodedContent.split("\\|");

            Map<String, String> result = new HashMap<>();
            if (parts.length >= 4) {
                result.put("courseId", parts[0]);
                result.put("teacherCode", parts[1]);
                result.put("classCode", parts[2]);
                result.put("timestamp", parts[3]);
                if (parts.length > 4) {
                    result.put("randomCode", parts[4]);
                }

                result.put("isMultiClass", "MULTI".equals(parts[2]) ? "true" : "false");
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("二维码内容解析失败：" + e.getMessage());
        }
    }

    public boolean isQrCodeValid(Long timestamp, int validSeconds) {
        if (timestamp == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = currentTime - timestamp;

        return timeDiff >= 0 && timeDiff <= validSeconds;
    }

    public int getRemainingTime(Long timestamp, int validSeconds) {
        if (timestamp == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = currentTime - timestamp;

        if (timeDiff < 0) {
            return validSeconds;
        } else if (timeDiff >= validSeconds) {
            return 0;
        } else {
            return (int) (validSeconds - timeDiff);
        }
    }

    public String generateQuestionnaireQrContent(Long paperId, String courseId, Long paperCourseSetId, Long timestamp) {
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("QUESTIONNAIRE|%d|%s|%d|%d|%s",
            paperId, courseId, paperCourseSetId, timestamp, randomCode);
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    public String generateQuestionnaireQrUrl(Long paperId, String courseId, Long paperCourseSetId, Long timestamp) {
        String content = generateQuestionnaireQrContent(paperId, courseId, paperCourseSetId, timestamp);
        return String.format("https://%s/signlab/student/questionnaire?qr=%s",this.domain, content);
    }

    public Map<String, String> parseQuestionnaireQrContent(String qrContent) {
        try {
            String decodedContent = new String(Base64.getDecoder().decode(qrContent));
            String[] parts = decodedContent.split("\\|");

            Map<String, String> result = new HashMap<>();
            if (parts.length >= 6 && "QUESTIONNAIRE".equals(parts[0])) {
                result.put("type", parts[0]);
                result.put("paperId", parts[1]);
                result.put("courseId", parts[2]);
                result.put("paperCourseSetId", parts[3]);
                result.put("timestamp", parts[4]);
                result.put("randomCode", parts[5]);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("问卷二维码内容解析失败：" + e.getMessage());
        }
    }
}