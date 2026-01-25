package com.example.demo.pojo.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TeacherQrVO {

    /** 有效秒数 */
    private Long seconds;

    /** 二维码密钥 */
    private String fileKey;
}
