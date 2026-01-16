package com.quiz.service;

import jakarta.mail.MessagingException;

/**
 * 邮件服务接口
 *
 * @author Quiz System
 * @since 2024-09-29
 */
public interface EmailService {

    /**
     * 发送HTML邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（HTML格式）
     * @throws MessagingException 邮件发送异常
     */
    void sendHtmlEmail(String to, String subject, String content) throws MessagingException;

    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @throws MessagingException 邮件发送异常
     */
    void sendVerificationCodeEmail(String to, String code) throws MessagingException;

    /**
     * 发送欢迎邮件
     * @param to 收件人邮箱
     * @param username 用户名
     * @throws MessagingException 邮件发送异常
     */
    void sendWelcomeEmail(String to, String username) throws MessagingException;

    /**
     * 发送密码重置邮件
     * @param to 收件人邮箱
     * @param username 用户名
     * @param newPassword 新密码
     * @throws MessagingException 邮件发送异常
     */
    void sendPasswordResetEmail(String to, String username, String newPassword) throws MessagingException;
}
