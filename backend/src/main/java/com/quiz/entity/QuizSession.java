package com.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 答题会话实体类
 *
 * @author Quiz System
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quiz_sessions")
public class QuizSession extends BaseEntity {

    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
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
     * 会话标识
     */
    private String sessionId;

    /**
     * 答题模式
     */
    private AnswerMode mode;

    /**
     * 总题数
     */
    private Integer totalQuestions;

    /**
     * 已答题数
     */
    private Integer answeredQuestions;

    /**
     * 正确题数
     */
    private Integer correctAnswers;

    /**
     * 当前题目索引（从0开始）
     */
    private Integer currentIndex;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 会话状态
     */
    private SessionStatus status;

    /**
     * 是否已删除（软删除）
     * 0-未删除，1-已删除
     */
    private Integer deleted;

    /**
     * 题库名称（非数据库字段）
     */
    @TableField(exist = false)
    private String bankName;

    /**
     * 用户名（非数据库字段）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 答题模式枚举
     */
    public enum AnswerMode {
        practice,  // 练习模式
        exam,      // 考试模式
        favorite,  // 收藏练习
        wrong      // 错题练习
    }

    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        ongoing,     // 进行中
        completed    // 已完成
    }
}
