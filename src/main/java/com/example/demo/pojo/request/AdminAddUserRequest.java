package com.example.demo.pojo.request;

import com.example.demo.enums.UserRole;
import com.example.demo.pojo.ao.RoleAO;
import com.example.demo.pojo.entity.User;
import com.tangzc.mpe.autotable.annotation.Column;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
public class AdminAddUserRequest {
    /** 用户名 */
    private String username;

    /** 姓名 */
    private String name;

    /** 角色 admin teacher student*/
    private String role;

    /** 院系 */
    private String department;

    /** 专业 */
    private String major;

    public User convertToUser(String password) {
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setRole(UserRole.fromCode(role).getCode());
        user.setPasswordSet(0);
        user.setDepartment(department);
        user.setMajor(major);
        user.setCreateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        user.setPassword(password);
        return user;
    }
}
