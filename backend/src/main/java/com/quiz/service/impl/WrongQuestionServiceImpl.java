package com.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.dto.WrongQuestionWithDetailDto;
import com.quiz.entity.Question;
import com.quiz.entity.QuestionBank;
import com.quiz.entity.WrongQuestion;
import com.quiz.mapper.WrongQuestionMapper;
import com.quiz.service.QuestionBankService;
import com.quiz.service.QuestionService;
import com.quiz.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题服务实现类
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionService {

    private final QuestionService questionService;
    private final QuestionBankService questionBankService;

    @Override
    public void addWrongQuestion(Long userId, Long bankId, Long questionId, String wrongAnswer) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId)
                   .eq(WrongQuestion::getQuestionId, questionId);

        WrongQuestion existingWrong = getOne(queryWrapper);

        if (existingWrong != null) {
            // 已存在，更新错误次数和错误答案
            existingWrong.setErrorCount(existingWrong.getErrorCount() + 1);
            existingWrong.setLastErrorTime(LocalDateTime.now());
            existingWrong.setLastErrorAnswer(wrongAnswer);
            existingWrong.setStatus(0); // 重新标记为未掌握
            updateById(existingWrong);
        } else {
            // 不存在，创建新记录
            WrongQuestion wrongQuestion = new WrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setBankId(bankId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setErrorCount(1);
            wrongQuestion.setLastErrorTime(LocalDateTime.now());
            wrongQuestion.setLastErrorAnswer(wrongAnswer);
            wrongQuestion.setStatus(0);
            save(wrongQuestion);
        }
    }

    @Override
    @Transactional
    public void markCorrect(Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId)
                   .eq(WrongQuestion::getQuestionId, questionId);

        WrongQuestion wrongQuestion = getOne(queryWrapper);
        if (wrongQuestion != null) {
            wrongQuestion.setStatus(1); // 标记为已掌握
            updateById(wrongQuestion);
        }
    }

    @Override
    public void markIncorrect(Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId)
                   .eq(WrongQuestion::getQuestionId, questionId);

        WrongQuestion wrongQuestion = getOne(queryWrapper);
        if (wrongQuestion != null) {
            wrongQuestion.setStatus(0); // 标记为未掌握
            wrongQuestion.setLastErrorTime(LocalDateTime.now());
            updateById(wrongQuestion);
        }
    }

    @Override
    public List<WrongQuestion> getUserWrongQuestions(Long userId, Integer status) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId);
        
        if (status != null) {
            queryWrapper.eq(WrongQuestion::getStatus, status);
        }
        
        queryWrapper.orderByDesc(WrongQuestion::getLastErrorTime);
        return list(queryWrapper);
    }

    @Override
    public long getUserWrongQuestionCount(Long userId, Integer status) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId);
        
        if (status != null) {
            queryWrapper.eq(WrongQuestion::getStatus, status);
        }
        
        return count(queryWrapper);
    }

    @Override
    public IPage<WrongQuestionWithDetailDto> getUserWrongQuestionsWithDetails(Long userId, int current, int size, Integer status, String keyword, Long bankId) {
        Page<WrongQuestion> page = new Page<>(current, size);
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId);

        if (status != null) {
            queryWrapper.eq(WrongQuestion::getStatus, status);
        }

        if (bankId != null) {
            queryWrapper.eq(WrongQuestion::getBankId, bankId);
        }

        queryWrapper.orderByDesc(WrongQuestion::getLastErrorTime);

        IPage<WrongQuestion> wrongQuestionPage = page(page, queryWrapper);

        // 获取所有涉及的题库ID
        Set<Long> bankIds = wrongQuestionPage.getRecords().stream()
            .map(WrongQuestion::getBankId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // 批量获取题库名称
        Map<Long, String> bankNameMap = new HashMap<>();
        if (!bankIds.isEmpty()) {
            List<QuestionBank> banks = questionBankService.listByIds(bankIds);
            bankNameMap = banks.stream()
                .collect(Collectors.toMap(QuestionBank::getId, QuestionBank::getName));
        }

        // 转换为包含题目详情的DTO
        IPage<WrongQuestionWithDetailDto> resultPage = new Page<>(current, size, wrongQuestionPage.getTotal());

        Map<Long, String> finalBankNameMap = bankNameMap;
        List<WrongQuestionWithDetailDto> dtoList = wrongQuestionPage.getRecords().stream().map(wrongQuestion -> {
            WrongQuestionWithDetailDto dto = new WrongQuestionWithDetailDto();
            BeanUtils.copyProperties(wrongQuestion, dto);

            // 设置题库名称
            if (wrongQuestion.getBankId() != null) {
                dto.setBankName(finalBankNameMap.getOrDefault(wrongQuestion.getBankId(), "未知题库"));
            }

            // 获取题目详情
            Question question = questionService.getById(wrongQuestion.getQuestionId());
            if (question != null) {
                WrongQuestionWithDetailDto.QuestionDetailDto questionDto = new WrongQuestionWithDetailDto.QuestionDetailDto();
                BeanUtils.copyProperties(question, questionDto);
                // 将枚举转换为字符串
                if (question.getType() != null) {
                    questionDto.setType(question.getType().name());
                }
                dto.setQuestion(questionDto);
            }

            return dto;
        }).toList();

        // 如果有关键词，过滤题目内容
        if (keyword != null && !keyword.trim().isEmpty()) {
            dtoList = dtoList.stream()
                .filter(dto -> {
                    if (dto.getQuestion() == null) return false;

                    String content = dto.getQuestion().getContent() != null ? dto.getQuestion().getContent() : "";

                    return content.contains(keyword);
                })
                .toList();

            // 重新计算总数
            resultPage.setTotal(dtoList.size());
        }

        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public void resetWrongQuestion(Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId)
                   .eq(WrongQuestion::getQuestionId, questionId);

        WrongQuestion wrongQuestion = getOne(queryWrapper);
        if (wrongQuestion != null) {
            wrongQuestion.setStatus(0); // 重置为未掌握
            updateById(wrongQuestion);
        }
    }

    @Override
    public List<WrongQuestion> getRandomWrongQuestions(Long userId, int count, Integer status) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId);

        if (status != null) {
            queryWrapper.eq(WrongQuestion::getStatus, status);
        }

        // 使用 last 方法添加原生SQL进行随机排序和限制数量
        queryWrapper.last("ORDER BY RAND() LIMIT " + count);
        return list(queryWrapper);
    }

    @Override
    public void batchUpdateStatus(Long userId, List<Long> questionIds, Integer status) {
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId)
                   .in(WrongQuestion::getQuestionId, questionIds);

        List<WrongQuestion> wrongQuestions = list(queryWrapper);

        for (WrongQuestion wrongQuestion : wrongQuestions) {
            wrongQuestion.setStatus(status);
            if (status == 0) {
                // 标记为未掌握
                wrongQuestion.setLastErrorTime(LocalDateTime.now());
            }
        }

        updateBatchById(wrongQuestions);
    }

    @Override
    public List<Long> getWrongQuestionIdsByBank(Long userId, Long bankId, Integer status) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId)
                   .eq(WrongQuestion::getBankId, bankId);

        if (status != null) {
            queryWrapper.eq(WrongQuestion::getStatus, status);
        }

        queryWrapper.select(WrongQuestion::getQuestionId);

        return list(queryWrapper).stream()
                .map(WrongQuestion::getQuestionId)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getWrongQuestionStatsByBank(Long userId) {
        // 获取用户所有错题
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId);
        List<WrongQuestion> allWrongQuestions = list(queryWrapper);

        // 按题库分组统计
        Map<Long, List<WrongQuestion>> groupedByBank = allWrongQuestions.stream()
            .filter(wq -> wq.getBankId() != null)
            .collect(Collectors.groupingBy(WrongQuestion::getBankId));

        // 获取所有题库名称
        Set<Long> bankIds = groupedByBank.keySet();
        Map<Long, String> bankNameMap = new HashMap<>();
        if (!bankIds.isEmpty()) {
            List<QuestionBank> banks = questionBankService.listByIds(bankIds);
            bankNameMap = banks.stream()
                .collect(Collectors.toMap(QuestionBank::getId, QuestionBank::getName));
        }

        // 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, String> finalBankNameMap = bankNameMap;
        groupedByBank.forEach((bankId, questions) -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("bankId", bankId);
            stat.put("bankName", finalBankNameMap.getOrDefault(bankId, "未知题库"));
            stat.put("total", questions.size());
            stat.put("masteredCount", questions.stream().filter(q -> q.getStatus() == 1).count());
            stat.put("unmasteredCount", questions.stream().filter(q -> q.getStatus() == 0).count());
            result.add(stat);
        });

        // 按未掌握数量排序
        result.sort((a, b) -> {
            Long countA = (Long) a.get("unmasteredCount");
            Long countB = (Long) b.get("unmasteredCount");
            return countB.compareTo(countA);
        });

        return result;
    }

    @Override
    public Map<String, Object> getWrongQuestionOverallStats(Long userId) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getUserId, userId);
        List<WrongQuestion> allWrongQuestions = list(queryWrapper);

        long total = allWrongQuestions.size();
        long mastered = allWrongQuestions.stream().filter(q -> q.getStatus() == 1).count();
        long unmastered = total - mastered;
        int accuracy = total > 0 ? (int) Math.round((mastered * 100.0) / total) : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("mastered", mastered);
        stats.put("unmastered", unmastered);
        stats.put("accuracy", accuracy);

        return stats;
    }
}
