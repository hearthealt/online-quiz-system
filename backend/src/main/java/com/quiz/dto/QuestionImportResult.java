package com.quiz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * 题目导入结果DTO
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportResult {
    private int totalCount;
    private int successCount;
    private int errorCount;
    private int skipCount;
    private String message;
    private List<String> errors = new ArrayList<>();

    // 保持向后兼容的构造器
    public QuestionImportResult(int successCount, int failCount, String message) {
        this.successCount = successCount;
        this.errorCount = failCount;
        this.message = message;
    }
}