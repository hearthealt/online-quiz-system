package com.quiz.strategy;

import com.quiz.dto.QuestionImportResult;

/**
 * 题目导入策略接口
 *
 * @author Quiz System
 * @since 2024
 */
public interface QuestionImportStrategy {

    /**
     * 导入题目到指定题库
     *
     * @param filePath 文件路径
     * @param bankId   题库ID
     * @param userId   用户ID
     * @return 导入结果
     */
    QuestionImportResult importQuestions(String filePath, Long bankId, Long userId);

    /**
     * 获取策略支持的文件扩展名
     *
     * @return 文件扩展名
     */
    String getSupportedExtension();

    /**
     * 验证文件格式
     *
     * @param filePath 文件路径
     * @return 是否支持
     */
    boolean supports(String filePath);
}
