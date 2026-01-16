package com.quiz.dto;

import lombok.Data;

/**
 * 题目导入请求DTO
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Data
public class QuestionImportRequest {
    private String filePath;
    private String fileName;
}