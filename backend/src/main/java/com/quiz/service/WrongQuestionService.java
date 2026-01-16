package com.quiz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.dto.WrongQuestionWithDetailDto;
import com.quiz.entity.WrongQuestion;

import java.util.List;
import java.util.Map;

/**
 * 错题服务接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
public interface WrongQuestionService extends IService<WrongQuestion> {

    /**
     * 添加错题
     */
    void addWrongQuestion(Long userId, Long bankId, Long questionId, String wrongAnswer);

    /**
     * 标记为已掌握
     */
    void markCorrect(Long userId, Long questionId);

    /**
     * 标记为未掌握
     */
    void markIncorrect(Long userId, Long questionId);

    /**
     * 获取用户错题列表
     */
    List<WrongQuestion> getUserWrongQuestions(Long userId, Integer status);

    /**
     * 获取用户错题列表（分页，带题目详情）
     */
    IPage<WrongQuestionWithDetailDto> getUserWrongQuestionsWithDetails(Long userId, int current, int size, Integer status, String keyword, Long bankId);

    /**
     * 获取用户错题统计
     */
    long getUserWrongQuestionCount(Long userId, Integer status);

    /**
     * 获取按题库分组的错题统计
     */
    List<Map<String, Object>> getWrongQuestionStatsByBank(Long userId);

    /**
     * 获取错题总体统计
     */
    Map<String, Object> getWrongQuestionOverallStats(Long userId);

    /**
     * 重置错题状态
     */
    void resetWrongQuestion(Long userId, Long questionId);

    /**
     * 获取随机错题用于练习
     */
    List<WrongQuestion> getRandomWrongQuestions(Long userId, int count, Integer status);

    /**
     * 批量更新错题状态
     */
    void batchUpdateStatus(Long userId, List<Long> questionIds, Integer status);

    /**
     * 获取指定题库的错题题目ID列表
     */
    List<Long> getWrongQuestionIdsByBank(Long userId, Long bankId, Integer status);
}
