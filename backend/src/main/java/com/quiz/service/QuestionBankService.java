package com.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.entity.QuestionBank;

import java.util.List;

/**
 * 题库服务接口
 *
 * @author Quiz System
 * @since 2024
 */
public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 获取所有启用的题库列表（带用户进度）
     */
    List<QuestionBank> getEnabledBanksWithProgress(Long userId);

    /**
     * 根据名称查找题库
     */
    QuestionBank getByName(String name);

    /**
     * 创建题库（通过文件名）
     */
    QuestionBank createBank(String name, Long createdBy);

    /**
     * 更新题库信息
     */
    boolean updateBankInfo(Long bankId, String description, Integer status, Integer sortOrder);

    /**
     * 删除题库（同时删除所有题目）
     */
    boolean deleteBank(Long bankId);

    /**
     * 检查题库是否存在
     */
    boolean existsByName(String name);

    /**
     * 更新题库的题目数量
     */
    void updateQuestionCount(Long bankId);
}
