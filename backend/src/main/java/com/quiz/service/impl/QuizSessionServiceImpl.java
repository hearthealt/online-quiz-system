package com.quiz.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.entity.Question;
import com.quiz.entity.QuestionBank;
import com.quiz.entity.QuizSession;
import com.quiz.entity.UserAnswer;
import com.quiz.entity.User;
import com.quiz.mapper.QuizSessionMapper;
import com.quiz.service.QuestionBankService;
import com.quiz.service.QuestionService;
import com.quiz.service.QuizSessionService;
import com.quiz.service.UserAnswerService;
import com.quiz.service.UserService;
import com.quiz.service.WrongQuestionService;
import com.quiz.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 答题会话服务实现类
 *
 * @author Quiz System
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class QuizSessionServiceImpl extends ServiceImpl<QuizSessionMapper, QuizSession> implements QuizSessionService {

    private final QuestionService questionService;
    private final UserAnswerService userAnswerService;

    @Autowired
    @Lazy
    private QuestionBankService questionBankService;

    @Autowired
    @Lazy
    private WrongQuestionService wrongQuestionService;

    @Autowired
    @Lazy
    private FavoriteService favoriteService;

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public QuizSession getOngoingSession(Long userId, Long bankId, QuizSession.AnswerMode mode) {
        LambdaQueryWrapper<QuizSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuizSession::getUserId, userId)
                   .eq(QuizSession::getBankId, bankId)
                   .eq(QuizSession::getMode, mode)
                   .eq(QuizSession::getStatus, QuizSession.SessionStatus.ongoing)
                   .and(w -> w.isNull(QuizSession::getDeleted).or().eq(QuizSession::getDeleted, 0));
        return getOne(queryWrapper);
    }

    @Override
    public List<QuizSession> getAllOngoingSessions(Long userId) {
        LambdaQueryWrapper<QuizSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuizSession::getUserId, userId)
                   .eq(QuizSession::getStatus, QuizSession.SessionStatus.ongoing)
                   .and(w -> w.isNull(QuizSession::getDeleted).or().eq(QuizSession::getDeleted, 0))
                   .orderByDesc(QuizSession::getUpdatedAt);
        List<QuizSession> sessions = list(queryWrapper);

        // 填充题库名称
        for (QuizSession session : sessions) {
            if (session.getBankId() != null) {
                QuestionBank bank = questionBankService.getById(session.getBankId());
                if (bank != null) {
                    session.setBankName(bank.getName());
                }
            }
        }

        return sessions;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrContinueSession(Long userId, Long bankId, QuizSession.AnswerMode mode, boolean forceNew) {
        Map<String, Object> result = new HashMap<>();

        // 检查题库是否存在
        QuestionBank bank = questionBankService.getById(bankId);
        if (bank == null) {
            throw new RuntimeException("题库不存在");
        }

        // 根据模式获取题目
        List<Question> questions;
        if (mode == QuizSession.AnswerMode.favorite) {
            // 收藏练习模式：获取用户在该题库中收藏的题目
            List<Long> questionIds = favoriteService.getFavoriteQuestionIdsByBank(userId, bankId);
            if (questionIds.isEmpty()) {
                throw new RuntimeException("该题库没有收藏的题目");
            }
            questions = questionService.listByIds(questionIds);
        } else if (mode == QuizSession.AnswerMode.wrong) {
            // 错题练习模式：获取用户在该题库中的错题（未掌握的）
            List<Long> questionIds = wrongQuestionService.getWrongQuestionIdsByBank(userId, bankId, 0);
            if (questionIds.isEmpty()) {
                throw new RuntimeException("该题库没有错题");
            }
            questions = questionService.listByIds(questionIds);
        } else {
            // 普通练习/考试模式：获取题库所有题目
            questions = questionService.getQuestionsByBankId(bankId);
        }

        if (questions.isEmpty()) {
            throw new RuntimeException("没有可用的题目");
        }

        // 如果强制新建，删除旧的未完成会话
        if (forceNew) {
            resetSession(userId, bankId, mode);
        }

        // 检查是否有未完成的会话
        QuizSession session = getOngoingSession(userId, bankId, mode);
        List<UserAnswer> existingAnswers = new ArrayList<>();
        int startIndex = 0;

        if (session != null) {
            // 继续未完成的会话
            existingAnswers = userAnswerService.getAnswersBySessionId(session.getSessionId());
            startIndex = session.getCurrentIndex();

            // 对于错题和收藏模式，需要实时更新题目数量
            if ((mode == QuizSession.AnswerMode.wrong || mode == QuizSession.AnswerMode.favorite)
                && session.getTotalQuestions() != questions.size()) {
                session.setTotalQuestions(questions.size());
                updateById(session);
            }
        } else {
            // 创建新会话
            session = new QuizSession();
            session.setUserId(userId);
            session.setBankId(bankId);
            session.setSessionId(IdUtil.fastSimpleUUID());
            session.setMode(mode);
            session.setTotalQuestions(questions.size());
            session.setAnsweredQuestions(0);
            session.setCorrectAnswers(0);
            session.setCurrentIndex(0);
            session.setStartTime(LocalDateTime.now());
            session.setStatus(QuizSession.SessionStatus.ongoing);
            session.setDeleted(0);
            save(session);
        }

        session.setBankName(bank.getName());

        result.put("session", session);
        result.put("questions", questions);
        result.put("answers", existingAnswers);
        result.put("startIndex", startIndex);

        return result;
    }

    @Override
    public QuizSession getSessionBySessionId(String sessionId) {
        LambdaQueryWrapper<QuizSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuizSession::getSessionId, sessionId)
                   .and(w -> w.isNull(QuizSession::getDeleted).or().eq(QuizSession::getDeleted, 0));
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public Map<String, Object> submitAnswer(String sessionId, Long userId, Long questionId, int questionIndex, String userAnswer) {
        Map<String, Object> result = new HashMap<>();

        // 获取会话
        QuizSession session = getSessionBySessionId(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此会话");
        }
        if (session.getStatus() != QuizSession.SessionStatus.ongoing) {
            throw new RuntimeException("会话已结束");
        }

        // 获取题目信息
        Question question = questionService.getQuestionDetail(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        // 判断答案是否正确
        boolean isCorrect = checkAnswer(question, userAnswer);

        // 保存或更新用户答案
        UserAnswer answer = userAnswerService.getAnswer(sessionId, questionId);
        if (answer == null) {
            answer = new UserAnswer();
            answer.setUserId(userId);
            answer.setSessionId(sessionId);
            answer.setQuestionId(questionId);
            answer.setQuestionIndex(questionIndex);
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setCreatedAt(LocalDateTime.now());
            answer.setUpdatedAt(LocalDateTime.now());
            userAnswerService.save(answer);

            // 更新会话统计
            session.setAnsweredQuestions(session.getAnsweredQuestions() + 1);
            if (isCorrect) {
                session.setCorrectAnswers(session.getCorrectAnswers() + 1);
            }
        } else {
            // 更新已有答案
            boolean wasCorrect = answer.getIsCorrect() != null && answer.getIsCorrect() == 1;
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setUpdatedAt(LocalDateTime.now());
            userAnswerService.updateById(answer);

            // 更新正确数
            if (wasCorrect && !isCorrect) {
                session.setCorrectAnswers(session.getCorrectAnswers() - 1);
            } else if (!wasCorrect && isCorrect) {
                session.setCorrectAnswers(session.getCorrectAnswers() + 1);
            }
        }

        // 更新当前索引
        session.setCurrentIndex(questionIndex + 1);
        updateById(session);

        // 如果答错，添加到错题本
        if (!isCorrect) {
            wrongQuestionService.addWrongQuestion(userId, session.getBankId(), questionId, userAnswer);
        }

        // 检查是否还有下一题（根据索引）
        boolean hasNext = questionIndex + 1 < session.getTotalQuestions();

        // 检查是否所有题目都已回答（只有全部答完才结束会话）
        if (session.getAnsweredQuestions().equals(session.getTotalQuestions())) {
            completeSession(sessionId);
        }

        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", question.getCorrectAnswer());
        result.put("analysis", question.getAnalysis());
        result.put("currentIndex", session.getCurrentIndex());
        result.put("hasNext", hasNext);

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> submitExam(String sessionId, Long userId, List<Map<String, Object>> answers) {
        Map<String, Object> result = new HashMap<>();

        // 获取会话
        QuizSession session = getSessionBySessionId(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此会话");
        }
        if (session.getStatus() != QuizSession.SessionStatus.ongoing) {
            throw new RuntimeException("会话已结束");
        }

        // 获取所有题目
        List<Question> questions = questionService.getQuestionsByBankId(session.getBankId());
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<Map<String, Object>> results = new ArrayList<>();
        int correctCount = 0;

        for (Map<String, Object> answerData : answers) {
            Long questionId = Long.valueOf(answerData.get("questionId").toString());
            int questionIndex = Integer.parseInt(answerData.get("questionIndex").toString());
            String userAnswerStr = (String) answerData.get("userAnswer");

            Question question = questionMap.get(questionId);
            if (question == null) continue;

            boolean isCorrect = checkAnswer(question, userAnswerStr);
            if (isCorrect) {
                correctCount++;
            }

            // 保存答案
            UserAnswer userAnswer = userAnswerService.getAnswer(sessionId, questionId);
            if (userAnswer == null) {
                userAnswer = new UserAnswer();
                userAnswer.setUserId(userId);
                userAnswer.setSessionId(sessionId);
                userAnswer.setQuestionId(questionId);
                userAnswer.setQuestionIndex(questionIndex);
                userAnswer.setCreatedAt(LocalDateTime.now());
            }
            userAnswer.setUserAnswer(userAnswerStr);
            userAnswer.setIsCorrect(isCorrect ? 1 : 0);
            userAnswer.setUpdatedAt(LocalDateTime.now());
            userAnswerService.saveOrUpdate(userAnswer);

            // 如果答错，添加到错题本
            if (!isCorrect) {
                wrongQuestionService.addWrongQuestion(userId, session.getBankId(), questionId, userAnswerStr);
            }

            Map<String, Object> questionResult = new HashMap<>();
            questionResult.put("questionId", questionId);
            questionResult.put("isCorrect", isCorrect);
            questionResult.put("correctAnswer", question.getCorrectAnswer());
            questionResult.put("analysis", question.getAnalysis());
            results.add(questionResult);
        }

        // 更新会话统计
        session.setAnsweredQuestions(answers.size());
        session.setCorrectAnswers(correctCount);
        session.setCurrentIndex(session.getTotalQuestions());
        session.setEndTime(LocalDateTime.now());
        session.setStatus(QuizSession.SessionStatus.completed);
        updateById(session);

        result.put("session", session);
        result.put("results", results);

        return result;
    }

    @Override
    @Transactional
    public boolean resetSession(Long userId, Long bankId, QuizSession.AnswerMode mode) {
        // 查找未完成的会话
        QuizSession session = getOngoingSession(userId, bankId, mode);
        if (session != null) {
            // 软删除：将 deleted 标记为 1
            session.setDeleted(1);
            session.setStatus(QuizSession.SessionStatus.completed);
            session.setEndTime(LocalDateTime.now());
            updateById(session);
        }
        return true;
    }

    @Override
    public Map<String, Object> getSessionDetail(String sessionId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        QuizSession session = getSessionBySessionId(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此会话");
        }

        // 获取题库信息
        QuestionBank bank = questionBankService.getById(session.getBankId());
        if (bank != null) {
            session.setBankName(bank.getName());
        }

        // 根据模式获取题目列表
        List<Question> questions;
        QuizSession.AnswerMode mode = session.getMode();
        if (mode == QuizSession.AnswerMode.favorite) {
            // 收藏练习模式：获取用户在该题库中收藏的题目
            List<Long> questionIds = favoriteService.getFavoriteQuestionIdsByBank(userId, session.getBankId());
            questions = questionIds.isEmpty() ? new ArrayList<>() : questionService.listByIds(questionIds);
        } else if (mode == QuizSession.AnswerMode.wrong) {
            // 错题练习模式：获取用户在该题库中的错题（未掌握的）
            List<Long> questionIds = wrongQuestionService.getWrongQuestionIdsByBank(userId, session.getBankId(), 0);
            questions = questionIds.isEmpty() ? new ArrayList<>() : questionService.listByIds(questionIds);
        } else {
            // 普通练习/考试模式：获取题库所有题目
            questions = questionService.getQuestionsByBankId(session.getBankId());
        }

        // 对于错题和收藏模式，实时更新题目数量（会话进行中可能新增了错题/收藏）
        if ((mode == QuizSession.AnswerMode.wrong || mode == QuizSession.AnswerMode.favorite)
            && session.getStatus() == QuizSession.SessionStatus.ongoing
            && session.getTotalQuestions() != questions.size()) {
            session.setTotalQuestions(questions.size());
            updateById(session);
        }

        // 获取答题记录
        List<UserAnswer> answers = userAnswerService.getAnswersBySessionId(sessionId);

        result.put("session", session);
        result.put("questions", questions);
        result.put("answers", answers);

        return result;
    }

    @Override
    public List<QuizSession> getUserSessions(Long userId, int current, int size, String mode, String status) {
        LambdaQueryWrapper<QuizSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuizSession::getUserId, userId)
                   .and(w -> w.isNull(QuizSession::getDeleted).or().eq(QuizSession::getDeleted, 0));

        if (mode != null && !mode.isEmpty()) {
            queryWrapper.eq(QuizSession::getMode, QuizSession.AnswerMode.valueOf(mode));
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(QuizSession::getStatus, QuizSession.SessionStatus.valueOf(status));
        }

        queryWrapper.orderByDesc(QuizSession::getCreatedAt);

        // 简单分页
        int offset = (current - 1) * size;
        queryWrapper.last("LIMIT " + offset + ", " + size);

        return list(queryWrapper);
    }

    @Override
    public List<QuizSession> getRecentSessions(Long userId, int limit) {
        LambdaQueryWrapper<QuizSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuizSession::getUserId, userId)
                   .and(w -> w.isNull(QuizSession::getDeleted).or().eq(QuizSession::getDeleted, 0))
                   .orderByDesc(QuizSession::getCreatedAt)
                   .last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public QuizSession completeSession(String sessionId) {
        QuizSession session = getSessionBySessionId(sessionId);
        if (session != null && session.getStatus() == QuizSession.SessionStatus.ongoing) {
            session.setEndTime(LocalDateTime.now());
            session.setStatus(QuizSession.SessionStatus.completed);
            updateById(session);
        }
        return session;
    }

    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer == null) {
            return false;
        }

        // 根据题目类型进行不同的判断
        switch (question.getType()) {
            case single:
                // 单选题：直接比较（忽略大小写）
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());

            case judge:
                // 判断题：需要处理中英文格式转换
                // 前端传递的是 "true"/"false"，数据库可能存储 "正确"/"错误" 或 "true"/"false"
                String normalizedUserAnswer = normalizeJudgeAnswer(userAnswer.trim());
                String normalizedCorrectAnswer = normalizeJudgeAnswer(correctAnswer.trim());
                return normalizedUserAnswer.equals(normalizedCorrectAnswer);

            case multiple:
                // 多选题：支持两种格式 "ABC" 或 "A,B,C"
                // 统一去除逗号，转为字符数组排序后比较
                String userAnswerNormalized = userAnswer.toUpperCase().replace(",", "");
                String correctAnswerNormalized = correctAnswer.toUpperCase().replace(",", "");
                char[] userChars = userAnswerNormalized.toCharArray();
                char[] correctChars = correctAnswerNormalized.toCharArray();
                Arrays.sort(userChars);
                Arrays.sort(correctChars);
                return Arrays.equals(userChars, correctChars);

            case essay:
                // 简答题：简单比较（可以根据需求改进）
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());

            default:
                return false;
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

    @Override
    public Map<String, Object> getAdminSessionList(int current, int size, Long userId, Long bankId, String mode, String status, Integer deleted) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<QuizSession> queryWrapper = new LambdaQueryWrapper<>();

        // 可选条件
        if (userId != null) {
            queryWrapper.eq(QuizSession::getUserId, userId);
        }
        if (bankId != null) {
            queryWrapper.eq(QuizSession::getBankId, bankId);
        }
        if (mode != null && !mode.isEmpty()) {
            queryWrapper.eq(QuizSession::getMode, QuizSession.AnswerMode.valueOf(mode));
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(QuizSession::getStatus, QuizSession.SessionStatus.valueOf(status));
        }
        // deleted 字段过滤（不传则查全部，传0查未删除，传1查已删除）
        if (deleted != null) {
            queryWrapper.eq(QuizSession::getDeleted, deleted);
        }

        // 排序
        queryWrapper.orderByDesc(QuizSession::getCreatedAt);

        // 统计总数
        long total = count(queryWrapper);

        // 分页
        int offset = (current - 1) * size;
        queryWrapper.last("LIMIT " + offset + ", " + size);

        List<QuizSession> sessions = list(queryWrapper);

        // 填充题库名称和用户名
        for (QuizSession session : sessions) {
            QuestionBank bank = questionBankService.getById(session.getBankId());
            if (bank != null) {
                session.setBankName(bank.getName());
            }
            User user = userService.getById(session.getUserId());
            if (user != null) {
                session.setUsername(user.getUsername());
            }
        }

        result.put("records", sessions);
        result.put("total", total);
        result.put("current", current);
        result.put("size", size);

        return result;
    }
}
