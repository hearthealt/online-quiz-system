package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.WrongQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错题Mapper接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {
}
