package com.example.demo.pojo.request;

import com.tangzc.mpe.autotable.annotation.Column;
import lombok.Data;

@Data
public class GetUserRequest {

    private Long size;

    private Long current;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String name;

    /** 专业 */
    private String role;

    /** 院系 */
    private String department;

    /** 专业 */
    private String major;
}
