package com.quiz.config;

import com.quiz.config.properties.QuizSystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用程序配置类
 *
 * @author Quiz System
 * @since 2024-10-09
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final QuizSystemProperties quizSystemProperties;

    /**
     * 应用启动后的初始化任务
     */
    @Bean
    public ApplicationRunner applicationStartupRunner() {
        return args -> {
            log.info("=================================");
            log.info("答题系统配置加载完成");
            log.info("默认分页大小: {}", quizSystemProperties.getPage().getDefaultSize());
            log.info("最大分页大小: {}", quizSystemProperties.getPage().getMaxSize());
            log.info("最大批量操作数量: {}", quizSystemProperties.getQuestion().getMaxBatchSize());
            log.info("JWT过期时间: {} 秒", quizSystemProperties.getSecurity().getJwtExpireTime());
            log.info("文件上传路径: {}", quizSystemProperties.getFile().getUploadPath());
            log.info("=================================");

            // 初始化缓存
            initializeCache();

            // 预加载配置数据
            preloadConfigurationData();
        };
    }

    /**
     * 初始化缓存
     */
    private void initializeCache() {
        log.info("正在初始化缓存配置...");
        // 这里可以预热一些常用的缓存数据
        // 例如：分类列表、系统配置等
    }

    /**
     * 预加载配置数据
     */
    private void preloadConfigurationData() {
        log.info("正在预加载系统配置数据...");
        // 这里可以预加载一些系统级别的配置数据
        // 例如：系统设置、字典数据等
    }
}