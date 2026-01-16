package com.quiz.service;

import com.quiz.dto.QuestionImportResult;

import java.util.List;

/**
 * 题目导入服务接口
 *
 * @author Quiz System
 * @since 2024
 */
public interface QuestionImportService {

    /**
     * 导入题目到指定题库
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param bankId   题库ID
     * @param userId   用户ID
     * @return 导入结果
     */
    QuestionImportResult importQuestions(String filePath, String fileName, Long bankId, Long userId);

    /**
     * 获取支持的文件格式
     *
     * @return 支持的文件扩展名列表
     */
    List<String> getSupportedFormats();

    /**
     * 验证文件格式是否支持
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean isFormatSupported(String fileName);
}
