package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据ID获取用户信息（包含权限）
     * @param id 用户ID
     * @return 用户信息
     */
    User selectByIdWithPermissions(Long id);

    /**
     * 更新用户权限
     * @param userId 用户ID
     * @param permissions 权限列表
     */
    void updateUserPermissions(@Param("userId") Long userId, @Param("permissions") List<String> permissions);
}
