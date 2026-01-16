package com.quiz.service;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 *
 * @author Quiz System
 * @since 2024-09-28
 */
public interface StatisticsService {

    /**
     * 获取系统统计数据
     */
    Map<String, Object> getSystemStats();

    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStats();

    /**
     * 获取排行榜数据
     */
    List<Map<String, Object>> getRankingData(String type, String period);

    /**
     * 获取详细统计数据
     */
    Map<String, Object> getDetailedStats();

    /**
     * 获取答题趋势数据
     */
    List<Map<String, Object>> getAnswerTrend(String period);

    /**
     * 获取题型统计数据
     */
    List<Map<String, Object>> getTypeStats();

    /**
     * 获取今日学习数据
     */
    Map<String, Object> getTodayStats();

    /**
     * 获取学习进度
     */
    Map<String, Object> getLearningProgress();

    /**
     * 获取学习历史
     */
    List<Map<String, Object>> getLearningHistory(String startDate, String endDate, int page, int size);

    /**
     * 导出统计报告
     */
    String exportStatisticsReport(String format);
}