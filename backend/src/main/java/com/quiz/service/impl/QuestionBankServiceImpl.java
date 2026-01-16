package com.quiz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.entity.QuestionBank;
import com.quiz.entity.QuizSession;
import com.quiz.mapper.QuestionBankMapper;
import com.quiz.service.QuestionBankService;
import com.quiz.service.QuizSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题库服务实现类
 *
 * @author Quiz System
 * @since 2024
 */
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Autowired
    @Lazy
    private QuizSessionService quizSessionService;

    @Override
    public List<QuestionBank> getEnabledBanksWithProgress(Long userId) {
        List<QuestionBank> banks = baseMapper.findAllEnabled();

        if (userId != null && !banks.isEmpty()) {
            // 为每个题库获取用户的进度
            for (QuestionBank bank : banks) {
                // 获取练习进度
                QuizSession practiceSession = quizSessionService.getOngoingSession(
                    userId, bank.getId(), QuizSession.AnswerMode.practice);
                if (practiceSession != null) {
                    bank.setPracticeProgress(practiceSession.getAnsweredQuestions());
                }

                // 获取考试进度
                QuizSession examSession = quizSessionService.getOngoingSession(
                    userId, bank.getId(), QuizSession.AnswerMode.exam);
                if (examSession != null) {
                    bank.setExamProgress(examSession.getAnsweredQuestions());
                }
            }
        }

        return banks;
    }

    @Override
    public QuestionBank getByName(String name) {
        return baseMapper.findByName(name);
    }

    @Override
    @Transactional
    public QuestionBank createBank(String name, Long createdBy) {
        QuestionBank bank = new QuestionBank();
        bank.setName(name);
        bank.setQuestionCount(0);
        bank.setStatus(1);
        bank.setSortOrder(0);
        bank.setCreatedBy(createdBy);
        save(bank);
        return bank;
    }

    @Override
    @Transactional
    public boolean updateBankInfo(Long bankId, String description, Integer status, Integer sortOrder) {
        QuestionBank bank = getById(bankId);
        if (bank == null) {
            return false;
        }

        if (description != null) {
            bank.setDescription(description);
        }
        if (status != null) {
            bank.setStatus(status);
        }
        if (sortOrder != null) {
            bank.setSortOrder(sortOrder);
        }

        return updateById(bank);
    }

    @Override
    @Transactional
    public boolean deleteBank(Long bankId) {
        // 由于外键级联删除，题目会自动删除
        return removeById(bankId);
    }

    @Override
    public boolean existsByName(String name) {
        return baseMapper.findByName(name) != null;
    }

    @Override
    public void updateQuestionCount(Long bankId) {
        baseMapper.updateQuestionCount(bankId);
    }
}
