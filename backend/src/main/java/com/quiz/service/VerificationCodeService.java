package com.quiz.service;

/**
 * 验证码服务接口
 *
 * @author Quiz System
 * @since 2024-09-29
 */
public interface VerificationCodeService {

    /**
     * 生成并发送验证码
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String email);

    /**
     * 验证验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code);

    /**
     * 删除验证码
     * @param email 邮箱地址
     */
    void deleteCode(String email);
}
