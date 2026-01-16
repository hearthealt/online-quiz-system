package com.quiz.controller;

import com.quiz.dto.Result;
import com.quiz.entity.QuizSession;
import com.quiz.service.QuizSessionService;
import com.quiz.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 答题会话控制器
 * 支持练习模式和考试模式，支持断点续答
 *
 * @author Quiz System
 * @since 2024
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final QuizSessionService sessionService;

    /**
     * 开始或继续答题会话
     * @param request 包含 bankId, mode(practice/exam), forceNew(可选，是否强制新建)
     */
    @PostMapping("/start")
    public Result<Map<String, Object>> startSession(@RequestBody Map<String, Object> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Long bankId = Long.valueOf(request.get("bankId").toString());
            String modeStr = (String) request.get("mode");
            Boolean forceNew = request.get("forceNew") != null && Boolean.parseBoolean(request.get("forceNew").toString());

            QuizSession.AnswerMode mode = QuizSession.AnswerMode.valueOf(modeStr);

            Map<String, Object> result = sessionService.createOrContinueSession(userId, bankId, mode, forceNew);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否有未完成的会话
     * 如果不提供参数，返回所有未完成的会话
     * 如果提供bankId和mode，返回特定的未完成会话
     */
    @GetMapping("/ongoing")
    public Result<?> getOngoingSession(
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String mode) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 如果提供了bankId和mode，返回特定会话
            if (bankId != null && mode != null) {
                QuizSession.AnswerMode answerMode = QuizSession.AnswerMode.valueOf(mode);
                QuizSession session = sessionService.getOngoingSession(userId, bankId, answerMode);
                return Result.success("获取成功", session);
            }

            // 否则返回所有未完成的会话
            List<QuizSession> sessions = sessionService.getAllOngoingSessions(userId);
            return Result.success("获取成功", sessions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交单题答案（练习模式）
     */
    @PostMapping("/{sessionId}/answer")
    public Result<Map<String, Object>> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Long questionId = Long.valueOf(request.get("questionId").toString());
            int questionIndex = Integer.parseInt(request.get("questionIndex").toString());
            String userAnswer = (String) request.get("userAnswer");

            Map<String, Object> result = sessionService.submitAnswer(sessionId, userId, questionId, questionIndex, userAnswer);
            return Result.success("提交成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量提交答案（考试模式）
     */
    @PostMapping("/{sessionId}/submit")
    public Result<Map<String, Object>> submitExam(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) request.get("answers");

            Map<String, Object> result = sessionService.submitExam(sessionId, userId, answers);
            return Result.success("提交成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置会话（删除进度，重新开始）
     */
    @PostMapping("/reset")
    public Result<Void> resetSession(@RequestBody Map<String, Object> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Long bankId = Long.valueOf(request.get("bankId").toString());
            String modeStr = (String) request.get("mode");
            QuizSession.AnswerMode mode = QuizSession.AnswerMode.valueOf(modeStr);

            boolean success = sessionService.resetSession(userId, bankId, mode);
            if (success) {
                return Result.success("重置成功");
            } else {
                return Result.error("重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{sessionId}")
    public Result<QuizSession> getSession(@PathVariable String sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            QuizSession session = sessionService.getSessionBySessionId(sessionId);
            if (session == null) {
                return Result.error("会话不存在");
            }

            // 检查权限
            if (!session.getUserId().equals(userId)) {
                return Result.error("无权访问此会话");
            }

            return Result.success("获取成功", session);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取会话详细信息（包含所有题目和答题记录）
     */
    @GetMapping("/{sessionId}/detail")
    public Result<Map<String, Object>> getSessionDetail(@PathVariable String sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Map<String, Object> detail = sessionService.getSessionDetail(sessionId, userId);
            return Result.success("获取成功", detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成会话
     */
    @PutMapping("/{sessionId}/complete")
    public Result<QuizSession> completeSession(@PathVariable String sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            QuizSession session = sessionService.getSessionBySessionId(sessionId);
            if (session == null) {
                return Result.error("会话不存在");
            }

            if (!session.getUserId().equals(userId)) {
                return Result.error("无权操作此会话");
            }

            QuizSession completedSession = sessionService.completeSession(sessionId);
            return Result.success("会话已完成", completedSession);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping
    public Result<List<QuizSession>> getUserSessions(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<QuizSession> sessions = sessionService.getUserSessions(userId, current, size, mode, status);
            return Result.success("获取成功", sessions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取最近的会话
     */
    @GetMapping("/recent")
    public Result<List<QuizSession>> getRecentSessions(@RequestParam(defaultValue = "10") int limit) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<QuizSession> sessions = sessionService.getRecentSessions(userId, limit);
            return Result.success("获取成功", sessions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            QuizSession session = sessionService.getSessionBySessionId(sessionId);
            if (session == null) {
                return Result.error("会话不存在");
            }

            // 检查权限
            if (!session.getUserId().equals(userId)) {
                return Result.error("无权删除此会话");
            }

            boolean success = sessionService.removeById(session.getId());
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
