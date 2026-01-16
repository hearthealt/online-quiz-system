package com.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.entity.QuizSession;

import java.util.List;
import java.util.Map;

/**
 * 答题会话服务接口
 *
 * @author Quiz System
 * @since 2024
 */
public interface QuizSessionService extends IService<QuizSession> {

    /**
     * 检查是否有未完成的会话
     */
    QuizSession getOngoingSession(Long userId, Long bankId, QuizSession.AnswerMode mode);

    /**
     * 获取用户所有未完成的会话
     */
    List<QuizSession> getAllOngoingSessions(Long userId);

    /**
     * 创建或获取答题会话
     * @param userId 用户ID
     * @param bankId 题库ID
     * @param mode 答题模式
     * @param forceNew 是否强制新建（重置）
     * @return 会话信息
     */
    Map<String, Object> createOrContinueSession(Long userId, Long bankId, QuizSession.AnswerMode mode, boolean forceNew);

    /**
     * 根据sessionId获取会话
     */
    QuizSession getSessionBySessionId(String sessionId);

    /**
     * 提交单题答案（练习模式）
     */
    Map<String, Object> submitAnswer(String sessionId, Long userId, Long questionId, int questionIndex, String userAnswer);

    /**
     * 批量提交答案（考试模式）
     */
    Map<String, Object> submitExam(String sessionId, Long userId, List<Map<String, Object>> answers);

    /**
     * 重置会话（删除进度，重新开始）
     */
    boolean resetSession(Long userId, Long bankId, QuizSession.AnswerMode mode);

    /**
     * 获取会话详情（包含所有答题记录）
     */
    Map<String, Object> getSessionDetail(String sessionId, Long userId);

    /**
     * 获取用户的会话列表
     */
    List<QuizSession> getUserSessions(Long userId, int current, int size, String mode, String status);

    /**
     * 获取最近的会话
     */
    List<QuizSession> getRecentSessions(Long userId, int limit);

    /**
     * 完成会话
     */
    QuizSession completeSession(String sessionId);

    /**
     * 管理员查询所有会话（包括已删除的）
     */
    Map<String, Object> getAdminSessionList(int current, int size, Long userId, Long bankId, String mode, String status, Integer deleted);
}
