package com.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户答题记录实体类
 *
 * @author Quiz System
 * @since 2024
 */
@Data
@TableName("user_answers")
public class UserAnswer {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话标识
     */
    private String sessionId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 题目索引
     */
    private Integer questionIndex;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 是否正确（0-错误，1-正确，NULL-未判定）
     */
    private Integer isCorrect;

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
}
