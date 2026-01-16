package com.quiz.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 答题系统自定义配置属性
 *
 * @author Quiz System
 * @since 2024-10-09
 */
@Data
@Component
@ConfigurationProperties(prefix = "quiz.system")
public class QuizSystemProperties {

    /**
     * 分页配置
     */
    private PageConfig page = new PageConfig();

    /**
     * 题目配置
     */
    private QuestionConfig question = new QuestionConfig();

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 安全配置
     */
    private SecurityConfig security = new SecurityConfig();

    /**
     * 文件上传配置
     */
    private FileConfig file = new FileConfig();

    @Data
    public static class PageConfig {
        /**
         * 默认页面大小
         */
        private int defaultSize = 10;

        /**
         * 最大页面大小
         */
        private int maxSize = 100;

        /**
         * 游标分页的默认大小
         */
        private int cursorSize = 50;
    }

    @Data
    public static class QuestionConfig {
        /**
         * 单次批量操作的最大题目数量
         */
        private int maxBatchSize = 10000;

        /**
         * 随机题目的默认数量
         */
        private int defaultRandomCount = 20;

        /**
         * 随机题目的最大数量
         */
        private int maxRandomCount = 5000;

        /**
         * 题目内容的最大长度
         */
        private int maxContentLength = 2000;

        /**
         * 题目选项的最大数量
         */
        private int maxOptionsCount = 10;
    }

    @Data
    public static class CacheConfig {
        /**
         * 题目缓存过期时间（秒）
         */
        private long questionExpireTime = 3600;

        /**
         * 分类统计缓存过期时间（秒）
         */
        private long categoryStatsExpireTime = 1800;

        /**
         * 用户会话缓存过期时间（秒）
         */
        private long userSessionExpireTime = 7200;
    }

    @Data
    public static class SecurityConfig {
        /**
         * JWT过期时间（秒）
         */
        private long jwtExpireTime = 604800; // 7天

        /**
         * 密码最小长度
         */
        private int passwordMinLength = 6;

        /**
         * 密码最大长度
         */
        private int passwordMaxLength = 20;

        /**
         * 登录失败最大次数
         */
        private int maxLoginAttempts = 5;

        /**
         * 登录失败锁定时间（秒）
         */
        private long lockoutDuration = 1800; // 30分钟
    }

    @Data
    public static class FileConfig {
        /**
         * 文件上传的最大大小（MB）
         */
        private long maxFileSize = 10;

        /**
         * 支持的题目导入文件类型
         */
        private String[] allowedImportTypes = {"xlsx", "xls"};

        /**
         * 文件存储路径
         */
        private String uploadPath = "./uploads";

        /**
         * 临时文件保存时间（小时）
         */
        private int tempFileRetentionHours = 24;
    }
}