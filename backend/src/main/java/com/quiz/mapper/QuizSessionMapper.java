package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.QuizSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题会话Mapper接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Mapper
public interface QuizSessionMapper extends BaseMapper<QuizSession> {
}
