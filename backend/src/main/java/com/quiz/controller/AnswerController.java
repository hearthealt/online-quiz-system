package com.quiz.controller;

import com.quiz.dto.AnswerRequest;
import com.quiz.dto.Result;
import com.quiz.entity.UserAnswer;
import com.quiz.service.AnswerService;
import com.quiz.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 答题控制器
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 提交答案
     */
    @PostMapping
    public Result<UserAnswer> submitAnswer(@RequestBody @Validated AnswerRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            UserAnswer result = answerService.submitAnswer(userId, request);
            return Result.success("答案提交成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量提交答案
     */
    @PostMapping("/batch")
    public Result<List<UserAnswer>> submitAnswers(@RequestBody @Validated List<AnswerRequest> requests) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<UserAnswer> results = answerService.submitAnswers(userId, requests);
            return Result.success("答案批量提交成功", results);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取答题历史
     */
    @GetMapping("/history")
    public Result<List<UserAnswer>> getAnswerHistory(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "8") int size) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<UserAnswer> history = answerService.getUserAnswerHistory(userId, current, size);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取答题统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAnswerStats() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> stats = answerService.getUserAnswerStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取会话答题记录
     */
    @GetMapping("/session/{sessionId}")
    public Result<List<UserAnswer>> getSessionAnswers(@PathVariable String sessionId) {
        try {
            List<UserAnswer> answers = answerService.getSessionAnswers(sessionId);
            return Result.success(answers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
