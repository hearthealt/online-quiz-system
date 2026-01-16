package com.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（不返回给前端）
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户状态（0-禁用，1-正常）
     */
    private Integer status = 1;

    /**
     * 用户角色（admin-管理员，user-普通用户）
     */
    private String role = "user";

    /**
     * 用户权限（JSON格式存储）
     */
    private List<String> permissions;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        if ("admin".equals(this.role)) {
            return true; // 管理员拥有所有权限
        }
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 检查是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(this.role);
    }
}
