package com.quiz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quiz.dto.Result;
import com.quiz.dto.WrongQuestionWithDetailDto;
import com.quiz.entity.WrongQuestion;
import com.quiz.service.WrongQuestionService;
import com.quiz.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 错题控制器
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    /**
     * 获取错题列表（分页，带题目详情）
     */
    @GetMapping
    public Result<IPage<WrongQuestionWithDetailDto>> getWrongQuestions(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long bankId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            IPage<WrongQuestionWithDetailDto> wrongQuestions = wrongQuestionService.getUserWrongQuestionsWithDetails(userId, current, size, status, keyword, bankId);
            return Result.success(wrongQuestions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取错题统计
     */
    @GetMapping("/stats")
    public Result<Long> getWrongQuestionStats(@RequestParam(required = false) Integer status) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            long count = wrongQuestionService.getUserWrongQuestionCount(userId, status);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取按题库分组的错题统计
     */
    @GetMapping("/stats/by-bank")
    public Result<List<Map<String, Object>>> getWrongQuestionStatsByBank() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<Map<String, Object>> stats = wrongQuestionService.getWrongQuestionStatsByBank(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取错题总体统计
     */
    @GetMapping("/stats/overall")
    public Result<Map<String, Object>> getWrongQuestionOverallStats() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Map<String, Object> stats = wrongQuestionService.getWrongQuestionOverallStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记为已掌握
     */
    @PutMapping("/{questionId}/mastered")
    public Result<Void> markQuestionAsMastered(@PathVariable Long questionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            wrongQuestionService.markCorrect(userId, questionId);
            return Result.success("标记成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记为未掌握
     */
    @PutMapping("/{questionId}/unmastered")
    public Result<Void> markQuestionAsUnmastered(@PathVariable Long questionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            wrongQuestionService.markIncorrect(userId, questionId);
            return Result.success("标记成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置错题状态
     */
    @PutMapping("/{questionId}/reset")
    public Result<Void> resetWrongQuestion(@PathVariable Long questionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            wrongQuestionService.resetWrongQuestion(userId, questionId);
            return Result.success("重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 错题重做练习
     */
    @PostMapping("/retry")
    public Result<Void> retryWrongQuestions(@RequestBody List<Long> questionIds) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 实现错题重做逻辑，将错题标记为重做状态
            for (Long questionId : questionIds) {
                wrongQuestionService.resetWrongQuestion(userId, questionId);
            }
            return Result.success("开始错题练习");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取随机错题用于练习
     */
    @GetMapping("/random")
    public Result<List<WrongQuestion>> getRandomWrongQuestions(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<WrongQuestion> wrongQuestions = wrongQuestionService.getRandomWrongQuestions(userId, count, status);
            return Result.success(wrongQuestions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量标记错题状态
     */
    @PutMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@RequestBody BatchUpdateStatusRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            wrongQuestionService.batchUpdateStatus(userId, request.getIds(), request.getStatus());
            return Result.success("批量更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新状态请求类
     */
    public static class BatchUpdateStatusRequest {
        private List<Long> ids;
        private Integer status;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
