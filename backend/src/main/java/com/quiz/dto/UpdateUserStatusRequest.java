package com.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户状态请求DTO
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Data
@Schema(description = "更新用户状态请求")
public class UpdateUserStatusRequest {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}
