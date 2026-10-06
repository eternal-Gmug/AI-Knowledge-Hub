-- =============================================================
-- AI-Knowledge-Hub 初始化 SQL（5 张表）
-- 兼容 MySQL 8.0
-- =============================================================

CREATE DATABASE IF NOT EXISTS `ai_knowledge_hub`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `ai_knowledge_hub`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`   VARCHAR(64)  NOT NULL COMMENT '用户名',
  `password`   VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码',
  `nickname`   VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 知识文章表
CREATE TABLE IF NOT EXISTS `knowledge` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT       NOT NULL COMMENT '作者 ID',
  `title`      VARCHAR(255) NOT NULL COMMENT '标题',
  `content`    LONGTEXT     NOT NULL COMMENT 'Markdown 正文',
  `summary`    TEXT         DEFAULT NULL COMMENT 'AI 摘要',
  `status`     VARCHAR(16)  NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED/DELETED',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识文章表';

-- 3. 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id`   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(64) NOT NULL COMMENT '标签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 4. 知识-标签关联表（复合主键）
CREATE TABLE IF NOT EXISTS `knowledge_tag` (
  `knowledge_id` BIGINT NOT NULL COMMENT '知识 ID',
  `tag_id`       BIGINT NOT NULL COMMENT '标签 ID',
  PRIMARY KEY (`knowledge_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识-标签关联表';

-- 5. 文档表
CREATE TABLE IF NOT EXISTS `document` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT       NOT NULL COMMENT '所属用户 ID',
  `filename`   VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path`  VARCHAR(512) NOT NULL COMMENT '本地存储路径',
  `content`    LONGTEXT     DEFAULT NULL COMMENT '文档文本内容',
  `status`     VARCHAR(16)  NOT NULL DEFAULT 'READY' COMMENT '状态: READY/DELETED',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';
