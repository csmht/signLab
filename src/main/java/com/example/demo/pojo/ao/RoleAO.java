ackage com.example.demo.pojo.ao;

import com.example.demo.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色应用对象
 * 用于存储线程级别的角色信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private UserRole role;

    /**
     * 用户ID
     */
    private Long userId;

    public RoleAO(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }
}