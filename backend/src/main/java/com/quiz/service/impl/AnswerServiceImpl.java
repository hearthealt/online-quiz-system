package com.quiz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.dto.AnswerRequest;
import com.quiz.entity.Question;
import com.quiz.entity.UserAnswer;
import com.quiz.mapper.UserAnswerMapper;
import com.quiz.service.AnswerService;
import com.quiz.service.QuestionService;
import com.quiz.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答题服务实现类
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements AnswerService {

    private final QuestionService questionService;
    private final WrongQuestionService wrongQuestionService;

    @Override
    @Transactional
    public UserAnswer submitAnswer(Long userId, AnswerRequest request) {
        // 获取题目信息
        Question question = questionService.getById(request.getQuestionId());
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        // 判断答案是否正确
        boolean isCorrect = checkAnswer(question, request.getUserAnswer());

        // 创建答题记录
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUserId(userId);
        userAnswer.setQuestionId(request.getQuestionId());
        userAnswer.setUserAnswer(request.getUserAnswer());
        userAnswer.setIsCorrect(isCorrect ? 1 : 0);
        userAnswer.setSessionId(request.getSessionId());
        userAnswer.setCreatedAt(LocalDateTime.now());

        // 保存答题记录
        save(userAnswer);

        // 处理错题
        handleWrongQuestion(userId, question.getBankId(), request.getQuestionId(), isCorrect, request.getUserAnswer());

        return userAnswer;
    }

    @Override
    public List<UserAnswer> getUserAnswerHistory(Long userId, int current, int size) {
        Page<UserAnswer> page = new Page<>(current, size);
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAnswer::getUserId, userId)
                   .orderByDesc(UserAnswer::getCreatedAt);
        
        return page(page, queryWrapper).getRecords();
    }

    @Override
    public Map<String, Object> getUserAnswerStats(Long userId) {
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAnswer::getUserId, userId);

        List<UserAnswer> allAnswers = list(queryWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnswers", allAnswers.size());

        long correctAnswers = allAnswers.stream()
                .filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1)
                .count();
        stats.put("correctAnswers", correctAnswers);

        double accuracy = allAnswers.isEmpty() ? 0.0 : (double) correctAnswers / allAnswers.size() * 100;
        stats.put("accuracy", Math.round(accuracy * 100.0) / 100.0);

        return stats;
    }

    @Override
    @Transactional
    public List<UserAnswer> submitAnswers(Long userId, List<AnswerRequest> requests) {
        List<UserAnswer> results = new ArrayList<>();
        for (AnswerRequest request : requests) {
            UserAnswer userAnswer = submitAnswer(userId, request);
            results.add(userAnswer);
        }
        return results;
    }

    @Override
    public List<UserAnswer> getSessionAnswers(String sessionId) {
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAnswer::getSessionId, sessionId)
                   .orderByAsc(UserAnswer::getCreatedAt);
        return list(queryWrapper);
    }

    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String userAnswer) {
        if (StrUtil.isBlank(userAnswer) || StrUtil.isBlank(question.getCorrectAnswer())) {
            return false;
        }

        switch (question.getType()) {
            case single:
                // 单选题：用户答案是选项内容，正确答案可能是选项标识或选项内容
                return checkSingleAnswer(question, userAnswer.trim());
            
            case multiple:
                // 多选题：用户答案是JSON数组，正确答案可能是选项标识字符串或JSON数组
                return checkMultipleAnswer(question, userAnswer.trim());
            
            case judge:
                // 判断题：需要处理中英文格式转换
                // 前端传递的是 "true"/"false"，数据库可能存储 "正确"/"错误" 或 "true"/"false"
                return normalizeJudgeAnswer(userAnswer.trim()).equals(
                    normalizeJudgeAnswer(question.getCorrectAnswer().trim()));

            case essay:
                // 简答题：关键词匹配（简化实现）
                String[] keywords = question.getCorrectAnswer().split("[，,、\\s]+");
                for (String keyword : keywords) {
                    if (userAnswer.contains(keyword.trim())) {
                        return true;
                    }
                }
                return false;
            
            default:
                return false;
        }
    }

    /**
     * 检查单选题答案
     */
    private boolean checkSingleAnswer(Question question, String userAnswer) {
        String correctAnswer = question.getCorrectAnswer().trim();
        List<String> options = question.getOptions();
        
        // 如果正确答案是选项内容，直接比较
        if (options != null && options.contains(correctAnswer)) {
            return userAnswer.equals(correctAnswer);
        }
        
        // 如果正确答案是选项标识（A, B, C, D），转换为选项内容后比较
        if (correctAnswer.length() == 1 && correctAnswer.matches("[A-Z]")) {
            int index = correctAnswer.charAt(0) - 'A';
            if (options != null && index >= 0 && index < options.size()) {
                return userAnswer.equals(options.get(index));
            }
        }
        
        // 直接字符串比较作为后备方案
        return userAnswer.equals(correctAnswer);
    }

    /**
     * 检查多选题答案
     * 支持用户答案格式：ABC 或 A,B,C
     * 支持正确答案格式：ABC 或 A,B,C 或 JSON数组
     */
    private boolean checkMultipleAnswer(Question question, String userAnswer) {
        String correctAnswer = question.getCorrectAnswer().trim();

        try {
            // 标准化用户答案：去除逗号，转大写，排序
            String userNormalized = userAnswer.toUpperCase().replace(",", "");
            char[] userChars = userNormalized.toCharArray();
            java.util.Arrays.sort(userChars);

            // 标准化正确答案
            String correctNormalized;
            if (correctAnswer.startsWith("[") && correctAnswer.endsWith("]")) {
                // JSON数组格式，提取字母
                List<String> correctList = parseJsonArray(correctAnswer);
                StringBuilder sb = new StringBuilder();
                List<String> options = question.getOptions();
                for (String item : correctList) {
                    // 查找选项对应的字母
                    if (options != null) {
                        int idx = options.indexOf(item);
                        if (idx >= 0) {
                            sb.append((char) ('A' + idx));
                        }
                    }
                }
                correctNormalized = sb.toString().toUpperCase();
            } else {
                // ABC 或 A,B,C 格式
                correctNormalized = correctAnswer.toUpperCase().replace(",", "");
            }

            char[] correctChars = correctNormalized.toCharArray();
            java.util.Arrays.sort(correctChars);

            return java.util.Arrays.equals(userChars, correctChars);

        } catch (Exception e) {
            // 解析失败，直接字符串比较
            return userAnswer.equalsIgnoreCase(correctAnswer);
        }
    }

    /**
     * 解析JSON数组字符串
     */
    private List<String> parseJsonArray(String jsonStr) {
        try {
            // 简单的JSON数组解析
            if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                String content = jsonStr.substring(1, jsonStr.length() - 1);
                String[] items = content.split(",");
                List<String> result = new ArrayList<>();
                for (String item : items) {
                    result.add(item.trim().replaceAll("^\"|\"$", ""));
                }
                return result;
            }
        } catch (Exception e) {
            // 解析失败，返回空列表
        }
        return new ArrayList<>();
    }

    /**
     * 处理错题逻辑
     */
    private void handleWrongQuestion(Long userId, Long bankId, Long questionId, boolean isCorrect, String userAnswer) {
        if (!isCorrect) {
            // 答错了，添加到错题本
            wrongQuestionService.addWrongQuestion(userId, bankId, questionId, userAnswer);
        } else {
            // 答对了，检查是否需要更新错题状态
            wrongQuestionService.markCorrect(userId, questionId);
        }
    }

    /**
     * 标准化判断题答案，将各种格式统一为 "true" 或 "false"
     * 支持：true/false, 正确/错误, 对/错, 是/否, 1/0, T/F, Y/N
     */
    private String normalizeJudgeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String normalized = answer.trim().toLowerCase();
        // 判断为"正确"的情况
        if (normalized.equals("true") || normalized.equals("正确") ||
            normalized.equals("对") || normalized.equals("是") ||
            normalized.equals("1") || normalized.equals("t") || normalized.equals("y")) {
            return "true";
        }
        // 判断为"错误"的情况
        if (normalized.equals("false") || normalized.equals("错误") ||
            normalized.equals("错") || normalized.equals("否") ||
            normalized.equals("0") || normalized.equals("f") || normalized.equals("n")) {
            return "false";
        }
        // 无法识别时返回原值
        return normalized;
    }
}
