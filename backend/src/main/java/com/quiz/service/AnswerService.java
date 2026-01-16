package com.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.dto.AnswerRequest;
import com.quiz.entity.UserAnswer;

import java.util.List;
import java.util.Map;

/**
 * 答题服务接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
public interface AnswerService extends IService<UserAnswer> {

    /**
     * 提交答案
     */
    UserAnswer submitAnswer(Long userId, AnswerRequest request);

    /**
     * 获取用户答题历史
     */
    List<UserAnswer> getUserAnswerHistory(Long userId, int current, int size);

    /**
     * 获取用户答题统计
     */
    Map<String, Object> getUserAnswerStats(Long userId);

    /**
     * 批量提交答案
     */
    List<UserAnswer> submitAnswers(Long userId, List<AnswerRequest> requests);

    /**
     * 获取会话答题记录
     */
    List<UserAnswer> getSessionAnswers(String sessionId);
}
