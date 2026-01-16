package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 题库Mapper接口
 *
 * @author Quiz System
 * @since 2024
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 根据名称查找题库
     */
    @Select("SELECT * FROM question_banks WHERE name = #{name}")
    QuestionBank findByName(@Param("name") String name);

    /**
     * 获取所有启用的题库（按排序顺序）
     */
    @Select("SELECT * FROM question_banks WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<QuestionBank> findAllEnabled();

    /**
     * 更新题库的题目数量
     */
    @Update("UPDATE question_banks SET question_count = (SELECT COUNT(*) FROM questions WHERE bank_id = #{bankId} AND status = 1) WHERE id = #{bankId}")
    void updateQuestionCount(@Param("bankId") Long bankId);
}
