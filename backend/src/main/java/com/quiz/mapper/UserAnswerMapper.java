package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.UserAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户答题记录Mapper接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Mapper
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {
}
