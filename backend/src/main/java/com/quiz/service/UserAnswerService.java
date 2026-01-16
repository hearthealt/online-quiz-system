package com.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.entity.UserAnswer;

import java.util.List;

/**
 * 用户答题记录服务接口
 *
 * @author Quiz System
 * @since 2024
 */
public interface UserAnswerService extends IService<UserAnswer> {

    /**
     * 根据会话ID获取所有答题记录
     */
    List<UserAnswer> getAnswersBySessionId(String sessionId);

    /**
     * 获取单个答案
     */
    UserAnswer getAnswer(String sessionId, Long questionId);

    /**
     * 删除会话的所有答题记录
     */
    boolean deleteBySessionId(String sessionId);

    /**
     * 保存或更新答案
     */
    boolean saveOrUpdateAnswer(UserAnswer answer);
}
