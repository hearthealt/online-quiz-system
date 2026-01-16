package com.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 联系管理员请求DTO
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Data
@Schema(description = "联系管理员请求")
public class ContactRequest {

    @NotBlank(message = "问题类型不能为空")
    @Schema(description = "问题类型", example = "password", allowableValues = {"password", "account", "feature", "other"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "用户邮箱", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Schema(description = "用户名（可选）", example = "testuser", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String username;

    @NotBlank(message = "问题标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度应为5-100个字符")
    @Schema(description = "问题标题", example = "密码相关问题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @NotBlank(message = "问题描述不能为空")
    @Size(min = 10, max = 1000, message = "描述长度应为10-1000个字符")
    @Schema(description = "问题描述", example = "我忘记了密码，需要重置", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
}
