package com.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新用户权限请求DTO
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Data
@Schema(description = "更新用户权限请求")
public class UpdatePermissionsRequest {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "权限列表", example = "[\"question:create\", \"question:edit\"]")
    private List<String> permissions;
}
