package com.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送验证码请求DTO
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Data
@Schema(description = "发送验证码请求")
public class SendVerificationCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "test@example.com")
    private String email;
}
