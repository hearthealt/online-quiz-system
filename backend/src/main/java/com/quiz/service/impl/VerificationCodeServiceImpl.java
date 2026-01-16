package com.quiz.service.impl;

import com.quiz.service.EmailService;
import com.quiz.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    @Value("${verification.code.length:6}")
    private int codeLength;

    @Value("${verification.code.expire:300}")
    private int expireSeconds;

    @Value("${verification.code.prefix:verification:email:}")
    private String keyPrefix;

    @Override
    public boolean sendVerificationCode(String email) {
        try {
            // 检查是否在冷却期内（1分钟内只能发送一次）
            String cooldownKey = keyPrefix + "cooldown:" + email;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                log.warn("验证码发送过于频繁，邮箱：{}", email);
                return false;
            }

            // 生成验证码
            String code = generateCode();
            
            // 存储验证码到Redis
            String key = keyPrefix + email;
            redisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
            
            // 设置冷却期
            redisTemplate.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);
            
            // 发送邮件
            emailService.sendVerificationCodeEmail(email, code);
            
            log.info("验证码发送成功，邮箱：{}", email);
            return true;
        } catch (Exception e) {
            log.error("发送验证码失败，邮箱：{}，错误：{}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        try {
            String key = keyPrefix + email;
            String storedCode = redisTemplate.opsForValue().get(key);
            
            if (storedCode == null) {
                log.warn("验证码不存在或已过期，邮箱：{}", email);
                return false;
            }
            
            boolean isValid = storedCode.equals(code);
            if (isValid) {
                // 验证成功后删除验证码
                redisTemplate.delete(key);
                log.info("验证码验证成功，邮箱：{}", email);
            } else {
                log.warn("验证码错误，邮箱：{}", email);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("验证码验证失败，邮箱：{}，错误：{}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteCode(String email) {
        try {
            String key = keyPrefix + email;
            redisTemplate.delete(key);
            log.info("验证码已删除，邮箱：{}", email);
        } catch (Exception e) {
            log.error("删除验证码失败，邮箱：{}，错误：{}", email, e.getMessage());
        }
    }

    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
