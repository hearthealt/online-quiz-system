package com.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 重置密码请求DTO
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
