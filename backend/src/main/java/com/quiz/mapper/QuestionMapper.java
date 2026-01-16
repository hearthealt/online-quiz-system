package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 题目Mapper接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 随机获取题目
     */
    List<Question> selectRandomQuestions(int count);

    /**
     * 根据分类随机获取题目
     */
    List<Question> selectRandomQuestionsByCategory(String category, int count);

    /**
     * 根据难度随机获取题目
     */
    List<Question> selectRandomQuestionsByDifficulty(Integer difficulty, int count);

    /**
     * 根据ID获取题目详情
     */
    Question selectQuestionById(Long id);
}
