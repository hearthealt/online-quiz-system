package com.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 错题实体类
 *
 * @author Quiz System
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wrong_questions")
public class WrongQuestion extends BaseEntity {

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
     * 题库ID
     */
    private Long bankId;

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
}
