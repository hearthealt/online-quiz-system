package com.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目实体类
 *
 * @author Quiz System
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "questions", autoResultMap = true)
public class Question extends BaseEntity {

    /**
     * 题目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属题库ID
     */
    private Long bankId;

    /**
     * 题目类型
     */
    private QuestionType type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 选项内容（JSON格式）
     */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quiz.handler.StringListTypeHandler.class)
    @com.fasterxml.jackson.annotation.JsonProperty("options")
    private List<String> options;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 答案解析
     */
    private String analysis;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 题目状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 获取选项，如果为null则返回空列表
     */
    public List<String> getOptions() {
        if (this.options == null) {
            // 对于判断题和简答题，返回空列表
            if (this.type == QuestionType.judge || this.type == QuestionType.essay) {
                return new ArrayList<>();
            }
            return new ArrayList<>();
        }
        return this.options;
    }

    /**
     * 题目类型枚举
     */
    public enum QuestionType {
        single,    // 单选题
        multiple,  // 多选题
        judge,     // 判断题
        essay      // 简答题
    }
}
