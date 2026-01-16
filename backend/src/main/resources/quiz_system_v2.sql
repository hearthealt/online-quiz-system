/*
 在线答题系统 v2.0 数据库结构

 主要变更：
 1. 新增题库表 (question_banks)
 2. 题目表改为关联题库，删除分类/难度/分值等字段
 3. 会话表支持断点续答
 4. 简化用户答题记录表
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users (保持不变)
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(100) NULL COMMENT '昵称',
  `email` varchar(100) NULL COMMENT '邮箱',
  `avatar` varchar(500) NULL COMMENT '头像URL',
  `role` enum('admin','user') NOT NULL DEFAULT 'user' COMMENT '用户角色',
  `permissions` text NULL COMMENT '用户权限（JSON格式）',
  `status` tinyint DEFAULT 1 COMMENT '用户状态（0-禁用，1-正常）',
  `last_login_time` datetime NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`),
  UNIQUE INDEX `idx_email` (`email`),
  INDEX `idx_role` (`role`),
  INDEX `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

-- 插入默认管理员
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$FyiGhEEjsEcPeymquK6VdO0Vjhl8gCOOmte1XR3OPyprDIfbl.t/S', '管理员', 'admin@quiz.com', NULL, 'admin', '["bank:manage","question:manage"]', 1, NULL, NOW(), NOW());

-- ----------------------------
-- Table structure for question_banks (新增)
-- ----------------------------
DROP TABLE IF EXISTS `question_banks`;
CREATE TABLE `question_banks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题库ID',
  `name` varchar(100) NOT NULL COMMENT '题库名称（来自文件名）',
  `description` text NULL COMMENT '题库描述',
  `question_count` int DEFAULT 0 COMMENT '题目数量',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `sort_order` int DEFAULT 0 COMMENT '排序顺序',
  `created_by` bigint NULL COMMENT '创建者ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_name` (`name`),
  INDEX `idx_status` (`status`),
  INDEX `idx_sort_order` (`sort_order`),
  INDEX `idx_created_by` (`created_by`),
  CONSTRAINT `fk_bank_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题库表';

-- ----------------------------
-- Table structure for questions (重构)
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `bank_id` bigint NOT NULL COMMENT '所属题库ID',
  `type` enum('single','multiple','judge','essay') NOT NULL COMMENT '题目类型',
  `content` text NOT NULL COMMENT '题目内容',
  `options` json NULL COMMENT '选项内容（JSON数组）',
  `correct_answer` text NOT NULL COMMENT '正确答案',
  `analysis` text NULL COMMENT '答案解析',
  `sort_order` int DEFAULT 0 COMMENT '排序顺序',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `created_by` bigint NULL COMMENT '创建者ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_bank_id` (`bank_id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_bank_sort` (`bank_id`, `sort_order`),
  CONSTRAINT `fk_question_bank` FOREIGN KEY (`bank_id`) REFERENCES `question_banks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_question_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目表';

-- ----------------------------
-- Table structure for quiz_sessions (重构)
-- ----------------------------
DROP TABLE IF EXISTS `quiz_sessions`;
CREATE TABLE `quiz_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `bank_id` bigint NOT NULL COMMENT '题库ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话标识',
  `mode` enum('practice','exam') NOT NULL COMMENT '答题模式',
  `total_questions` int NOT NULL COMMENT '总题数',
  `answered_questions` int DEFAULT 0 COMMENT '已答题数',
  `correct_answers` int DEFAULT 0 COMMENT '正确题数',
  `current_index` int DEFAULT 0 COMMENT '当前题目索引',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime NULL COMMENT '结束时间',
  `status` enum('ongoing','completed') DEFAULT 'ongoing' COMMENT '会话状态',
  `deleted` tinyint DEFAULT 0 COMMENT '是否已删除（0-未删除，1-已删除）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_session_id` (`session_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_bank_id` (`bank_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_mode` (`mode`),
  INDEX `idx_user_bank_mode_status` (`user_id`, `bank_id`, `mode`, `status`),
  INDEX `idx_deleted` (`deleted`),
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_session_bank` FOREIGN KEY (`bank_id`) REFERENCES `question_banks` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '答题会话表';

-- ----------------------------
-- Table structure for user_answers (重构)
-- ----------------------------
DROP TABLE IF EXISTS `user_answers`;
CREATE TABLE `user_answers` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话标识',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_index` int NOT NULL COMMENT '题目索引',
  `user_answer` text NULL COMMENT '用户答案',
  `is_correct` tinyint NULL COMMENT '是否正确（NULL-未判定）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_session_question` (`session_id`, `question_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_question_id` (`question_id`),
  CONSTRAINT `fk_answer_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户答题记录表';

-- ----------------------------
-- Table structure for wrong_questions (重构)
-- ----------------------------
DROP TABLE IF EXISTS `wrong_questions`;
CREATE TABLE `wrong_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `bank_id` bigint NOT NULL COMMENT '题库ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `error_count` int DEFAULT 1 COMMENT '错误次数',
  `last_error_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后错误时间',
  `last_error_answer` text NULL COMMENT '最后错误答案',
  `status` tinyint DEFAULT 0 COMMENT '状态（0-未掌握，1-已掌握）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_user_question` (`user_id`, `question_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_bank_id` (`bank_id`),
  INDEX `idx_question_id` (`question_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_wrong_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wrong_bank` FOREIGN KEY (`bank_id`) REFERENCES `question_banks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wrong_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '错题表';

-- ----------------------------
-- Table structure for favorites (重构)
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `bank_id` bigint NOT NULL COMMENT '题库ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `notes` text NULL COMMENT '收藏备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_user_question` (`user_id`, `question_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_bank_id` (`bank_id`),
  INDEX `idx_question_id` (`question_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_bank` FOREIGN KEY (`bank_id`) REFERENCES `question_banks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 升级脚本（已有数据库执行此SQL添加deleted字段）
-- ----------------------------
-- ALTER TABLE `quiz_sessions` ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '是否已删除（0-未删除，1-已删除）' AFTER `status`;
-- ALTER TABLE `quiz_sessions` ADD INDEX `idx_deleted` (`deleted`);
