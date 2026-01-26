ackage com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表
 * 存储学生和教师的基础信息，包含院系和专业信息
 */
@Data
@AutoTable
@Table(value = "users", comment = "用户表 - 存储学生和教师的基础信息，包含院系和专业信息")
@TableName("users")
@TableIndex(name = "uk_username", fields = {"username"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_users_role", fields = {"role"})
@TableIndex(name = "idx_wx_unionid", fields = {"wxUnionid"})
@TableIndex(name = "idx_users_department", fields = {"department"})
@TableIndex(name = "idx_users_major", fields = {"major"})
public class User {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    @Column(comment = "用户名", type = "varchar(50)", notNull = true)
    private String username;

    /** 姓名 */
    @Column(comment = "姓名", type = "varchar(100)", notNull = true)
    private String name;

    /** 密码 */
    @Column(comment = "密码", type = "varchar(255)")
    private String password;

    /** 角色 */
    @Column(comment = "角色", type = "enum('student', 'teacher', 'admin')", notNull = true, defaultValue = "'student'")
    private String role;

    /** 是否已设置密码：0-未设置，1-已设置 */
    @Column(comment = "是否已设置密码：0-未设置，1-已设置", type = "tinyint", defaultValue = "0")
    private Integer passwordSet;

    /** 院系 */
    @Column(comment = "院系", type = "varchar(100)")
    private String department;

    /** 专业 */
    @Column(comment = "专业", type = "varchar(100)")
    private String major;

    /** 微信OpenID */
    @Column(comment = "微信OpenID", type = "varchar(100)")
    private String wxOpenid;

    /** 微信UnionID */
    @Column(comment = "微信UnionID", type = "varchar(100)")
    private String wxUnionid;

    /** 微信昵称 */
    @Column(comment = "微信昵称", type = "varchar(100)")
    private String wxNickname;

    /** 微信头像URL地址 */
    @Column(comment = "微信头像URL地址", type = "varchar(500)")
    private String wxAvatar;

    /** 微信账号绑定时间 */
    @Column(comment = "微信账号绑定时间", type = "datetime")
    private LocalDateTime wxBindTime;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}