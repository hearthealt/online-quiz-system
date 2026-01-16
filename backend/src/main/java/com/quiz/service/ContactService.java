package com.quiz.service;

import com.quiz.dto.ContactRequest;

/**
 * 联系服务接口
 *
 * @author Quiz System
 * @since 2024-09-29
 */
public interface ContactService {

    /**
     * 发送联系邮件
     * @param request 联系请求
     */
    void sendContactEmail(ContactRequest request);

    /**
     * 获取管理员邮箱
     * @return 管理员邮箱地址
     */
    String getAdminEmail();
}
