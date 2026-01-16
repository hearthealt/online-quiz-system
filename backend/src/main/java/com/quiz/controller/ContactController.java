package com.quiz.controller;

import com.quiz.dto.ContactRequest;
import com.quiz.dto.Result;
import com.quiz.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 联系管理员控制器
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@Tag(name = "联系管理", description = "用户联系管理员相关接口")
public class ContactController {

    private final ContactService contactService;

    /**
     * 发送联系邮件
     */
    @Operation(summary = "发送联系邮件", description = "用户向管理员发送问题反馈或请求帮助")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发送成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "发送失败",
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/send")
    public Result<Void> sendContactEmail(@RequestBody @Validated ContactRequest request) {
        try {
            contactService.sendContactEmail(request);
            return Result.success("邮件发送成功，我们会在24小时内回复您");
        } catch (Exception e) {
            return Result.badRequest("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员邮箱
     */
    @Operation(summary = "获取管理员邮箱", description = "获取系统管理员邮箱地址")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/admin-email")
    public Result<String> getAdminEmail() {
        try {
            String adminEmail = contactService.getAdminEmail();
            return Result.success("获取成功", adminEmail);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}
