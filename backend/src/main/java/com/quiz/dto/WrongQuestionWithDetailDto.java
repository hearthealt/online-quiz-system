package com.quiz.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题详情DTO
 *
 * @author Quiz System
 * @since 2024
 */
@Data
public class WrongQuestionWithDetailDto {

    /**
     * 错题记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题库ID
     */
    private Long bankId;

    /**
     * 题库名称
     */
    private String bankName;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 错误次数
     */
    private Integer errorCount;

    /**
     * 最后错误时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastErrorTime;

    /**
     * 最后错误答案
     */
    private String lastErrorAnswer;

    /**
     * 状态（0-未掌握，1-已掌握）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 题目详情
     */
    private QuestionDetailDto question;

    /**
     * 题目详情内部类
     */
    @Data
    public static class QuestionDetailDto {
        /**
         * 题目ID
         */
        private Long id;

        /**
         * 题库ID
         */
        private Long bankId;

        /**
         * 题目内容
         */
        private String content;

        /**
         * 题目类型
         */
        private String type;

        /**
         * 正确答案
         */
        private String correctAnswer;

        /**
         * 答案解析
         */
        private String analysis;

        /**
         * 选项
         */
        private List<String> options;
    }
}
