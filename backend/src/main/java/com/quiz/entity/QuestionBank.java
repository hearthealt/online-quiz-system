package com.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题库实体类
 *
 * @author Quiz System
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_banks")
public class QuestionBank extends BaseEntity {

    /**
     * 题库ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题库名称（来自文件名）
     */
    private String name;

    /**
     * 题库描述
     */
    private String description;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 用户练习进度（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private Integer practiceProgress;

    /**
     * 用户考试进度（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private Integer examProgress;
}
