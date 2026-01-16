package com.quiz.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quiz.annotation.RequirePermission;
import com.quiz.dto.Result;
import com.quiz.entity.Question;
import com.quiz.service.QuestionService;
import com.quiz.utils.SecurityUtils;
import com.quiz.utils.UserValidationHelper;
import com.quiz.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题目控制器
 * 简化版本，仅保留基本的题目管理功能
 * 题库相关操作请使用 QuestionBankController
 *
 * @author Quiz System
 * @since 2024
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "题目管理", description = "题目的增删改查接口")
public class QuestionController {

    private final QuestionService questionService;
    private final UserValidationHelper userValidationHelper;

    /**
     * 分页查询题目
     */
    @Operation(summary = "分页查询题目", description = "支持按题库ID、题型、关键字筛选")
    @GetMapping
    public Result<IPage<Question>> getQuestions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        // 按题库筛选
        if (bankId != null) {
            queryWrapper.eq(Question::getBankId, bankId);
        }

        // 按题型筛选
        if (type != null && !type.isEmpty()) {
            try {
                Question.QuestionType questionType = Question.QuestionType.valueOf(type);
                queryWrapper.eq(Question::getType, questionType);
            } catch (IllegalArgumentException ignored) {
                // 忽略无效的类型
            }
        }

        // 按关键字搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Question::getContent, keyword.trim());
        }

        // 只查询启用的题目，按排序号和ID排序
        queryWrapper.eq(Question::getStatus, 1)
                   .orderByAsc(Question::getSortOrder)
                   .orderByAsc(Question::getId);

        IPage<Question> result = questionService.page(page, queryWrapper);
        return Result.success(result);
    }

    /**
     * 获取题目详情
     */
    @Operation(summary = "获取题目详情", description = "根据ID获取题目详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "404", description = "题目不存在",
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/{id}")
    public Result<Question> getQuestion(@Parameter(description = "题目ID", example = "1") @PathVariable Long id) {
        Question question = questionService.getQuestionDetail(id);
        if (question == null) {
            throw new ResourceNotFoundException("题目不存在");
        }
        return Result.success(question);
    }

    /**
     * 批量获取题目详情
     */
    @PostMapping("/batch")
    public Result<List<Question>> getQuestionsByIds(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("题目ID列表不能为空");
            }

            if (ids.size() > 1000) {
                return Result.error("一次最多只能获取1000道题目");
            }

            List<Question> questions = questionService.getQuestionsByIds(ids);
            return Result.success(questions);
        } catch (Exception e) {
            return Result.error("获取题目失败: " + e.getMessage());
        }
    }

    /**
     * 创建题目（需要指定bankId）
     */
    @PostMapping
    @RequirePermission("question:create")
    public Result<Question> createQuestion(@RequestBody Question question) {
        // 获取当前用户ID
        Long userId = userValidationHelper.getCurrentUserIdOrThrow();

        // 验证bankId
        if (question.getBankId() == null) {
            return Result.error("必须指定题库ID");
        }

        // 设置创建者
        question.setCreatedBy(userId);
        question.setStatus(1);

        boolean success = questionService.addQuestion(question);
        if (!success) {
            throw new RuntimeException("创建题目失败");
        }

        return Result.success("创建成功", question);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    @RequirePermission("question:edit")
    public Result<Question> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        // 检查题目是否存在
        Question existingQuestion = questionService.getById(id);
        if (existingQuestion == null) {
            throw new ResourceNotFoundException("题目不存在");
        }

        // 获取当前用户ID并验证所有权
        Long userId = userValidationHelper.getCurrentUserAndValidateOwnership(
                existingQuestion.getCreatedBy(), "题目");

        question.setId(id);
        question.setCreatedBy(userId);
        // 保持原来的bankId
        question.setBankId(existingQuestion.getBankId());

        boolean success = questionService.updateById(question);
        if (!success) {
            throw new RuntimeException("更新题目失败");
        }

        return Result.success("更新成功", question);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    @RequirePermission("question:delete")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        // 检查题目是否存在
        Question existingQuestion = questionService.getById(id);
        if (existingQuestion == null) {
            throw new ResourceNotFoundException("题目不存在");
        }

        // 获取当前用户ID并验证所有权
        userValidationHelper.getCurrentUserAndValidateOwnership(
                existingQuestion.getCreatedBy(), "题目");

        boolean success = questionService.removeById(id);
        if (!success) {
            throw new RuntimeException("删除题目失败");
        }

        // 更新题库题目数量
        questionService.updateBankQuestionCount(existingQuestion.getBankId());

        return Result.success("删除成功");
    }

    /**
     * 批量启用题目
     */
    @PutMapping("/batch/enable")
    @RequirePermission("question:batch")
    public Result<Void> batchEnableQuestions(@RequestBody Map<String, List<Long>> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要启用的题目");
            }

            boolean success = questionService.batchUpdateStatus(ids, 1, userId);
            if (success) {
                return Result.success("批量启用成功");
            } else {
                return Result.error("批量启用失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量禁用题目
     */
    @PutMapping("/batch/disable")
    @RequirePermission("question:batch")
    public Result<Void> batchDisableQuestions(@RequestBody Map<String, List<Long>> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要禁用的题目");
            }

            boolean success = questionService.batchUpdateStatus(ids, 0, userId);
            if (success) {
                return Result.success("批量禁用成功");
            } else {
                return Result.error("批量禁用失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除题目
     */
    @DeleteMapping("/batch")
    @RequirePermission("question:batch")
    public Result<Void> batchDeleteQuestions(@RequestBody Map<String, List<Long>> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的题目");
            }

            boolean success = questionService.batchDelete(ids, userId);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
