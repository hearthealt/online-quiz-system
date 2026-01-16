package com.quiz.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏记录（带题目详情）DTO
 *
 * @author Quiz System
 * @since 2024
 */
@Data
public class FavoriteWithDetailDto {

    /**
     * 收藏记录ID
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
     * 题目ID
     */
    private Long questionId;

    /**
     * 收藏备注
     */
    private String notes;

    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 题目详情
     */
    private QuestionDetailDto question;

    /**
     * 题目详情DTO
     */
    @Data
    public static class QuestionDetailDto {
        private Long id;
        private Long bankId;
        private String content;
        private String type;
        private List<String> options;
        private String correctAnswer;
        private String analysis;
        private Integer sortOrder;
        private Integer status;
    }
}
