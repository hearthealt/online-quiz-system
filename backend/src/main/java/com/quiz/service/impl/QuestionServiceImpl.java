package com.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.entity.Question;
import com.quiz.entity.QuestionBank;
import com.quiz.mapper.QuestionBankMapper;
import com.quiz.mapper.QuestionMapper;
import com.quiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目服务实现类
 *
 * @author Quiz System
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionBankMapper questionBankMapper;

    @Override
    public List<Question> getQuestionsByBankId(Long bankId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getBankId, bankId)
                   .eq(Question::getStatus, 1)
                   .orderByAsc(Question::getSortOrder)
                   .orderByAsc(Question::getId);

        List<Question> questions = list(queryWrapper);
        return processQuestionsOptions(questions);
    }

    @Override
    public Question getQuestionDetail(Long id) {
        Question question = getById(id);
        if (question != null) {
            return processQuestionOptions(question);
        }
        return null;
    }

    @Override
    public List<Question> getQuestionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Question::getId, ids);

        List<Question> questions = list(queryWrapper);
        return processQuestionsOptions(questions);
    }

    @Override
    @Transactional
    public boolean addQuestion(Question question) {
        if (question.getBankId() == null) {
            throw new RuntimeException("题库ID不能为空");
        }

        question.setStatus(1);

        // 获取当前最大排序号
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getBankId, question.getBankId())
                   .orderByDesc(Question::getSortOrder)
                   .last("LIMIT 1");
        Question lastQuestion = getOne(queryWrapper);

        int sortOrder = lastQuestion != null ? lastQuestion.getSortOrder() + 1 : 0;
        question.setSortOrder(sortOrder);

        boolean saved = save(question);
        if (saved) {
            updateBankQuestionCount(question.getBankId());
        }
        return saved;
    }

    @Override
    @Transactional
    public Question addQuestion(Long bankId, Question question) {
        question.setBankId(bankId);
        question.setStatus(1);

        // 获取当前最大排序号
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getBankId, bankId)
                   .orderByDesc(Question::getSortOrder)
                   .last("LIMIT 1");
        Question lastQuestion = getOne(queryWrapper);

        int sortOrder = lastQuestion != null ? lastQuestion.getSortOrder() + 1 : 0;
        question.setSortOrder(sortOrder);

        save(question);
        return question;
    }

    @Override
    @Transactional
    public boolean deleteByBankId(Long bankId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getBankId, bankId);
        return remove(queryWrapper);
    }

    @Override
    @Transactional
    public boolean deleteQuestion(Long questionId) {
        return removeById(questionId);
    }

    @Override
    @Transactional
    public boolean reorderQuestions(Long bankId, List<Long> questionIds) {
        for (int i = 0; i < questionIds.size(); i++) {
            LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Question::getId, questionIds.get(i))
                        .eq(Question::getBankId, bankId)
                        .set(Question::getSortOrder, i);
            update(updateWrapper);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> ids, Integer status, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Question::getId, ids)
                    .set(Question::getStatus, status);

        // 如果需要权限检查
        if (userId != null) {
            updateWrapper.eq(Question::getCreatedBy, userId);
        }

        return update(updateWrapper);
    }

    @Override
    @Transactional
    public boolean batchDelete(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Question::getId, ids);

        // 如果需要权限检查
        if (userId != null) {
            queryWrapper.eq(Question::getCreatedBy, userId);
        }

        return remove(queryWrapper);
    }

    @Override
    public int countByBankId(Long bankId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getBankId, bankId)
                   .eq(Question::getStatus, 1);
        return (int) count(queryWrapper);
    }

    @Override
    public Map<String, Object> getQuestionStatsByBankId(Long bankId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取该题库所有启用的题目
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getBankId, bankId)
                   .eq(Question::getStatus, 1);
        List<Question> questions = list(queryWrapper);

        int total = questions.size();
        int singleCount = 0;
        int multipleCount = 0;
        int judgeCount = 0;
        int essayCount = 0;

        for (Question q : questions) {
            if (q.getType() != null) {
                switch (q.getType()) {
                    case single:
                        singleCount++;
                        break;
                    case multiple:
                        multipleCount++;
                        break;
                    case judge:
                        judgeCount++;
                        break;
                    case essay:
                        essayCount++;
                        break;
                }
            }
        }

        stats.put("totalQuestions", total);
        stats.put("singleCount", singleCount);
        stats.put("multipleCount", multipleCount);
        stats.put("judgeCount", judgeCount);
        stats.put("essayCount", essayCount);

        return stats;
    }

    @Override
    public void updateBankQuestionCount(Long bankId) {
        int count = countByBankId(bankId);
        QuestionBank bank = new QuestionBank();
        bank.setId(bankId);
        bank.setQuestionCount(count);
        questionBankMapper.updateById(bank);
    }

    /**
     * 处理题目选项，确保判断题和简答题的options为空列表而不是null
     */
    private List<Question> processQuestionsOptions(List<Question> questions) {
        return questions.stream().map(this::processQuestionOptions).collect(Collectors.toList());
    }

    /**
     * 处理单个题目的选项
     */
    private Question processQuestionOptions(Question question) {
        if (question.getOptions() == null) {
            question.setOptions(new ArrayList<>());
        }
        return question;
    }
}
