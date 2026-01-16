package com.quiz.controller;

import com.quiz.dto.Result;
import com.quiz.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 *
 * @author Quiz System
 * @since 2024-09-28
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "统计管理", description = "系统统计和用户统计相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取系统统计数据
     */
    @Operation(summary = "获取系统统计", description = "获取题库题目数、注册用户数、答题次数等系统统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class)))
    })
    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = statisticsService.getSystemStats();
            return Result.success("获取系统统计成功", stats);
        } catch (Exception e) {
            return Result.error("获取系统统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     */
    @Operation(summary = "获取用户统计", description = "获取当前用户的答题统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未登录", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class)))
    })
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = statisticsService.getUserStats();
            return Result.success("获取用户统计成功", stats);
        } catch (Exception e) {
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取排行榜数据
     */
    @Operation(summary = "获取排行榜", description = "获取用户答题排行榜数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class)))
    })
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getRankingData(
            @Parameter(description = "排行类型", example = "score") @RequestParam(required = false) String type,
            @Parameter(description = "时间范围", example = "all") @RequestParam(required = false) String period) {
        try {
            List<Map<String, Object>> ranking = statisticsService.getRankingData(type, period);
            return Result.success("获取排行榜成功", ranking);
        } catch (Exception e) {
            return Result.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取详细统计数据
     */
    @Operation(summary = "获取详细统计", description = "获取用户的详细答题统计数据")
    @GetMapping("/detailed")
    public Result<Map<String, Object>> getDetailedStats() {
        try {
            Map<String, Object> stats = statisticsService.getDetailedStats();
            return Result.success("获取详细统计成功", stats);
        } catch (Exception e) {
            return Result.error("获取详细统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取答题趋势数据
     */
    @Operation(summary = "获取答题趋势", description = "获取用户答题趋势数据")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getAnswerTrend(
            @Parameter(description = "时间周期", example = "week") @RequestParam String period) {
        try {
            List<Map<String, Object>> trend = statisticsService.getAnswerTrend(period);
            return Result.success("获取答题趋势成功", trend);
        } catch (Exception e) {
            return Result.error("获取答题趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取题型统计数据
     */
    @Operation(summary = "获取题型统计", description = "获取用户各题型的答题统计数据")
    @GetMapping("/type")
    public Result<List<Map<String, Object>>> getTypeStats() {
        try {
            List<Map<String, Object>> stats = statisticsService.getTypeStats();
            return Result.success("获取题型统计成功", stats);
        } catch (Exception e) {
            return Result.error("获取题型统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日学习数据
     */
    @Operation(summary = "获取今日学习数据", description = "获取用户今日的学习统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未登录", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class)))
    })
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStats() {
        try {
            Map<String, Object> stats = statisticsService.getTodayStats();
            return Result.success("获取今日学习数据成功", stats);
        } catch (Exception e) {
            return Result.error("获取今日学习数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习进度
     */
    @Operation(summary = "获取学习进度", description = "获取用户的学习进度数据，包括本周学习天数和连续学习天数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未登录", 
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Result.class)))
    })
    @GetMapping("/progress")
    public Result<Map<String, Object>> getLearningProgress() {
        try {
            Map<String, Object> progress = statisticsService.getLearningProgress();
            return Result.success("获取学习进度成功", progress);
        } catch (Exception e) {
            return Result.error("获取学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习历史
     */
    @Operation(summary = "获取学习历史", description = "获取用户的学习历史数据")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getLearningHistory(
            @Parameter(description = "开始日期", example = "2024-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2024-12-31") @RequestParam(required = false) String endDate,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, Object>> history = statisticsService.getLearningHistory(startDate, endDate, page, size);
            return Result.success("获取学习历史成功", history);
        } catch (Exception e) {
            return Result.error("获取学习历史失败: " + e.getMessage());
        }
    }

    /**
     * 导出统计报告
     */
    @Operation(summary = "导出统计报告", description = "导出用户的统计报告")
    @GetMapping("/export")
    public Result<String> exportStatisticsReport(
            @Parameter(description = "导出格式", example = "text") @RequestParam String format) {
        try {
            String report = statisticsService.exportStatisticsReport(format);
            return Result.success("导出统计报告成功", report);
        } catch (Exception e) {
            return Result.error("导出统计报告失败: " + e.getMessage());
        }
    }
}