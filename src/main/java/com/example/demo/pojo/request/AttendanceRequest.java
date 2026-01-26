ackage com.example.demo.pojo.request;

import lombok.Data;

/**
 * 签到请求DTO
 * 包含加密的二维码数据
 */
@Data
public class AttendanceRequest {

    /**
     * 加密的二维码数据
     */
    private String encryptedData;

    /**
     * 签到时的IP地址
     */
    private String ipAddress;
}