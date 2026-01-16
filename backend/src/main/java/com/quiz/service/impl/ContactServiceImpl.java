package com.quiz.service.impl;

import com.quiz.dto.ContactRequest;
import com.quiz.service.ContactService;
import com.quiz.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 联系服务实现类
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String adminEmail;

    @Override
    public void sendContactEmail(ContactRequest request) {
        try {
            // 构建邮件内容
            String subject = buildEmailSubject(request);
            String content = buildEmailContent(request);

            // 发送邮件给管理员
            emailService.sendHtmlEmail(adminEmail, subject, content);

            // 发送确认邮件给用户
            String userSubject = "问题反馈已收到 - Quiz System";
            String userContent = buildUserConfirmationEmail(request);
            emailService.sendHtmlEmail(request.getEmail(), userSubject, userContent);

            log.info("联系邮件发送成功：Type={}, Email={}, Subject={}", 
                    request.getType(), request.getEmail(), request.getSubject());

        } catch (MessagingException e) {
            log.error("发送联系邮件失败：{}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    @Override
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * 构建邮件主题
     */
    private String buildEmailSubject(ContactRequest request) {
        String typeLabel = getTypeLabel(request.getType());
        return String.format("[%s] %s - %s", typeLabel, request.getSubject(), request.getEmail());
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(ContactRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>用户问题反馈</h2>");
        content.append("<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse;'>");
        
        content.append("<tr><td><strong>问题类型</strong></td><td>").append(getTypeLabel(request.getType())).append("</td></tr>");
        content.append("<tr><td><strong>用户邮箱</strong></td><td>").append(request.getEmail()).append("</td></tr>");
        
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            content.append("<tr><td><strong>用户名</strong></td><td>").append(request.getUsername()).append("</td></tr>");
        }
        
        content.append("<tr><td><strong>问题标题</strong></td><td>").append(request.getSubject()).append("</td></tr>");
        content.append("<tr><td><strong>问题描述</strong></td><td>").append(request.getDescription().replace("\n", "<br>")).append("</td></tr>");
        content.append("<tr><td><strong>提交时间</strong></td><td>").append(java.time.LocalDateTime.now().toString()).append("</td></tr>");
        
        content.append("</table>");
        
        // 添加处理建议
        content.append("<h3>处理建议</h3>");
        content.append("<ul>");
        if ("password".equals(request.getType())) {
            content.append("<li>检查用户身份信息</li>");
            content.append("<li>重置用户密码</li>");
            content.append("<li>发送新密码到用户邮箱</li>");
        } else if ("account".equals(request.getType())) {
            content.append("<li>检查账号状态</li>");
            content.append("<li>解决登录问题</li>");
            content.append("<li>更新账号信息</li>");
        } else {
            content.append("<li>分析问题原因</li>");
            content.append("<li>提供解决方案</li>");
            content.append("<li>跟进处理结果</li>");
        }
        content.append("</ul>");
        
        content.append("</body></html>");
        return content.toString();
    }

    /**
     * 构建用户确认邮件内容
     */
    private String buildUserConfirmationEmail(ContactRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>问题反馈确认</h2>");
        content.append("<p>尊敬的用户，</p>");
        content.append("<p>我们已收到您的问题反馈：</p>");
        content.append("<ul>");
        content.append("<li><strong>问题类型：</strong>").append(getTypeLabel(request.getType())).append("</li>");
        content.append("<li><strong>问题标题：</strong>").append(request.getSubject()).append("</li>");
        content.append("<li><strong>提交时间：</strong>").append(java.time.LocalDateTime.now().toString()).append("</li>");
        content.append("</ul>");
        content.append("<p>我们会在24小时内处理您的问题并回复到您的邮箱。</p>");
        content.append("<p>如有紧急问题，请直接联系管理员。</p>");
        content.append("<p>感谢您的反馈！</p>");
        content.append("<p>Quiz System 团队</p>");
        content.append("</body></html>");
        return content.toString();
    }

    /**
     * 获取问题类型标签
     */
    private String getTypeLabel(String type) {
        switch (type) {
            case "password": return "密码问题";
            case "account": return "账号问题";
            case "feature": return "功能问题";
            case "other": return "其他问题";
            default: return "未知问题";
        }
    }
}
