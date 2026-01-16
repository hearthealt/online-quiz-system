package com.quiz.service.impl;

import com.quiz.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true表示内容是HTML
        mailSender.send(message);
        log.info("HTML邮件发送成功：To={}, Subject={}", to, subject);
    }

    @Override
    public void sendVerificationCodeEmail(String to, String code) throws MessagingException {
        String subject = "【在线答题系统】邮箱验证码";
        String content = buildVerificationEmailContent(code);
        sendHtmlEmail(to, subject, content);
        log.info("验证码邮件发送成功：To={}, Code={}", to, code);
    }

    @Override
    public void sendWelcomeEmail(String to, String username) throws MessagingException {
        String subject = "【在线答题系统】欢迎注册";
        String content = buildWelcomeEmailContent(username);
        sendHtmlEmail(to, subject, content);
        log.info("欢迎邮件发送成功：To={}, Username={}", to, username);
    }

    private String buildVerificationEmailContent(String code) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #409eff;">邮箱验证码</h2>
                    <p>亲爱的用户，您好！</p>
                    <p>您正在注册在线答题系统，验证码为：</p>
                    <div style="background-color: #f5f7fa; padding: 20px; text-align: center; margin: 20px 0;">
                        <span style="font-size: 24px; font-weight: bold; color: #409eff; letter-spacing: 2px;">%s</span>
                    </div>
                    <p><strong>验证码有效期为5分钟，请及时使用。</strong></p>
                    <p>如果这不是您的操作，请忽略此邮件。</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿回复。</p>
                    <p style="color: #999; font-size: 12px;">在线答题系统团队</p>
                </div>
            </body>
            </html>
            """, code);
    }

    private String buildWelcomeEmailContent(String username) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #409eff;">欢迎注册！</h2>
                    <p>亲爱的 <strong>%s</strong>，欢迎您！</p>
                    <p>恭喜您成功注册在线答题系统！</p>
                    <div style="background-color: #f0f9ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="color: #409eff; margin-top: 0;">现在您可以：</h3>
                        <ul style="margin: 0; padding-left: 20px;">
                            <li>浏览和练习各种题目</li>
                            <li>参与在线考试</li>
                            <li>查看学习统计</li>
                            <li>管理错题本</li>
                        </ul>
                    </div>
                    <p>祝您学习愉快！</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">在线答题系统团队</p>
                </div>
            </body>
            </html>
            """, username);
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String newPassword) throws MessagingException {
        String subject = "【在线答题系统】密码重置通知";
        String content = buildPasswordResetEmailContent(username, newPassword);
        sendHtmlEmail(to, subject, content);
        log.info("密码重置邮件发送成功：To={}, Username={}", to, username);
    }

    private String buildPasswordResetEmailContent(String username, String newPassword) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #e6a23c;">密码重置通知</h2>
                    <p>亲爱的 <strong>%s</strong>，您好！</p>
                    <p>您的账号密码已被管理员重置，新密码如下：</p>
                    <div style="background-color: #f5f7fa; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; border: 2px solid #e6a23c;">
                        <span style="font-size: 24px; font-weight: bold; color: #e6a23c; letter-spacing: 2px;">%s</span>
                    </div>
                    <div style="background-color: #fdf6ec; padding: 15px; border-radius: 8px; border-left: 4px solid #e6a23c; margin: 20px 0;">
                        <h3 style="color: #e6a23c; margin-top: 0;">重要提醒：</h3>
                        <ul style="margin: 0; padding-left: 20px;">
                            <li>请尽快登录系统并修改密码</li>
                            <li>建议使用强密码保护您的账号安全</li>
                            <li>如非本人操作，请立即联系管理员</li>
                        </ul>
                    </div>
                    <p>请妥善保管您的新密码，建议登录后立即修改为更安全的密码。</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿回复。</p>
                    <p style="color: #999; font-size: 12px;">在线答题系统团队</p>
                </div>
            </body>
            </html>
            """, username, newPassword);
    }
}
