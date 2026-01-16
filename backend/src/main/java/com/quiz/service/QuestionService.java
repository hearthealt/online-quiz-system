package com.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.entity.Question;

import java.util.List;
import java.util.Map;

/**
 * 题目服务接口
 *
 * @author Quiz System
 * @since 2024
 */
public interface QuestionService extends IService<Question> {

    /**
     * 获取题库下的所有题目（按顺序）
     */
    List<Question> getQuestionsByBankId(Long bankId);

    /**
     * 获取题目详情
     */
    Question getQuestionDetail(Long id);

    /**
     * 批量获取题目详情
     */
    List<Question> getQuestionsByIds(List<Long> ids);

    /**
     * 添加题目到题库（使用question中的bankId）
     */
    boolean addQuestion(Question question);

    /**
     * 添加题目到题库（指定bankId）
     */
    Question addQuestion(Long bankId, Question question);

    /**
     * 删除题库中的所有题目
     */
    boolean deleteByBankId(Long bankId);

    /**
     * 更新题库的题目数量
     */
    void updateBankQuestionCount(Long bankId);

    /**
     * 删除单个题目
     */
    boolean deleteQuestion(Long questionId);

    /**
     * 更新题目顺序
     */
    boolean reorderQuestions(Long bankId, List<Long> questionIds);

    /**
     * 批量更新题目状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status, Long userId);

    /**
     * 批量删除题目
     */
    boolean batchDelete(List<Long> ids, Long userId);

    /**
     * 获取题库的题目数量
     */
    int countByBankId(Long bankId);

    /**
     * 获取题库下各类题型数量统计
     */
    Map<String, Object> getQuestionStatsByBankId(Long bankId);
}
