package com.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.entity.UserAnswer;
import com.quiz.mapper.UserAnswerMapper;
import com.quiz.service.UserAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户答题记录服务实现类
 *
 * @author Quiz System
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {

    @Override
    public List<UserAnswer> getAnswersBySessionId(String sessionId) {
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAnswer::getSessionId, sessionId)
                   .orderByAsc(UserAnswer::getQuestionIndex);
        return list(queryWrapper);
    }

    @Override
    public UserAnswer getAnswer(String sessionId, Long questionId) {
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAnswer::getSessionId, sessionId)
                   .eq(UserAnswer::getQuestionId, questionId);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean deleteBySessionId(String sessionId) {
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAnswer::getSessionId, sessionId);
        return remove(queryWrapper);
    }

    @Override
    @Transactional
    public boolean saveOrUpdateAnswer(UserAnswer answer) {
        UserAnswer existing = getAnswer(answer.getSessionId(), answer.getQuestionId());
        if (existing != null) {
            answer.setId(existing.getId());
            return updateById(answer);
        } else {
            return save(answer);
        }
    }
}
