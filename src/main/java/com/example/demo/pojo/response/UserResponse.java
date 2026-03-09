package com.example.demo.pojo.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tangzc.mpe.autotable.annotation.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String name;

    /** 角色 */
    private String role;

    /** 院系 */
    private String department;

    /** 专业 */
    private String major;

    /** 创建时间 */
    private LocalDateTime createTime;
}
