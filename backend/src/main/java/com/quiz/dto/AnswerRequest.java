package com.quiz.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 答题请求DTO
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Data
public class AnswerRequest {

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 会话ID
     */
    private String sessionId;
}
