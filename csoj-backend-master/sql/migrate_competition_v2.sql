-- 团队竞赛系统数据库表迁移脚本 (MySQL 5.7+ 兼容版本)
-- 用于更新现有表结构以匹配新的实体定义
-- 创建时间: 2026-03-02
-- 版本: 1.0 -> 2.0
--
-- 注意: 请根据实际数据库情况选择执行相应的语句
-- 如果表不存在，请执行 team_competition.sql 创建表

-- 切换库
use oj_db;

-- ========================================
-- 方案A: 如果表已存在，执行以下迁移语句
-- ========================================

-- 1. 备份现有数据（可选但推荐）
-- CREATE TABLE competition_backup AS SELECT * FROM competition;
-- CREATE TABLE competition_registration_backup AS SELECT * FROM competition_registration;
-- CREATE TABLE competition_match_backup AS SELECT * FROM competition_match;
-- CREATE TABLE match_participation_backup AS SELECT * FROM match_participation;

-- 2. 删除并重建 competition 表（如果结构不匹配）
-- 警告: 这会删除现有数据！请先备份！

DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞赛ID',
    `name` varchar(200) NOT NULL COMMENT '比赛名称',
    `description` text COMMENT '比赛描述',
    `type` varchar(50) NOT NULL COMMENT '比赛类型：elimination/round_robin/team_ac/battle/relay',
    `status` int DEFAULT 0 COMMENT '状态：0-未开始, 1-报名中, 2-进行中, 3-已结束',
    `cover` varchar(512) COMMENT '封面图片',
    `creator_id` bigint NOT NULL COMMENT '创建者ID',
    `max_teams` int DEFAULT 16 COMMENT '最大参赛队伍数',
    `current_teams` int DEFAULT 0 COMMENT '当前报名队伍数',
    `min_team_size` int DEFAULT 1 COMMENT '每队最少人数',
    `max_team_size` int DEFAULT 5 COMMENT '每队最多人数',
    `register_start_time` datetime COMMENT '报名开始时间',
    `register_end_time` datetime COMMENT '报名截止时间',
    `start_time` datetime NOT NULL COMMENT '比赛开始时间',
    `end_time` datetime COMMENT '比赛结束时间',
    `time_limit` int DEFAULT 60 COMMENT '单场时间限制(分钟)',
    `is_public` tinyint DEFAULT 1 COMMENT '是否公开',
    `question_ids` text COMMENT '关联的题目ID列表(JSON)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type` (`type`),
    INDEX `idx_time` (`start_time`, `end_time`),
    INDEX `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛表';

-- 3. 删除并重建 competition_registration 表
DROP TABLE IF EXISTS `competition_registration`;
CREATE TABLE `competition_registration` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `competition_id` bigint NOT NULL COMMENT '竞赛ID',
    `team_id` bigint NOT NULL COMMENT '团队ID',
    `register_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `status` int DEFAULT 1 COMMENT '状态：0-待审核, 1-已确认, 2-已取消',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_comp_team` (`competition_id`, `team_id`),
    INDEX `idx_team` (`team_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛报名表';

-- 4. 删除并重建 competition_match 表
DROP TABLE IF EXISTS `competition_match`;
CREATE TABLE `competition_match` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对局ID',
    `competition_id` bigint NOT NULL COMMENT '竞赛ID',
    `round` int NOT NULL COMMENT '轮次',
    `match_number` int NOT NULL COMMENT '场次',
    `team_a_id` bigint NOT NULL COMMENT 'A队ID',
    `team_b_id` bigint COMMENT 'B队ID（轮空时为null）',
    `team_a_score` int DEFAULT 0 COMMENT 'A队得分',
    `team_b_score` int DEFAULT 0 COMMENT 'B队得分',
    `winner_id` bigint COMMENT '获胜队ID',
    `status` int DEFAULT 0 COMMENT '状态：0-未开始, 1-进行中, 2-已结束',
    `start_time` datetime COMMENT '开始时间',
    `end_time` datetime COMMENT '结束时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_competition` (`competition_id`),
    INDEX `idx_round` (`competition_id`, `round`),
    INDEX `idx_teams` (`team_a_id`, `team_b_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛对局表';

-- 5. 删除并重建 match_participation 表
DROP TABLE IF EXISTS `match_participation`;
CREATE TABLE `match_participation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `match_id` bigint NOT NULL COMMENT '对局ID',
    `team_id` bigint NOT NULL COMMENT '所属团队ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `solved_count` int DEFAULT 0 COMMENT '解决题目数',
    `total_time` bigint DEFAULT 0 COMMENT '总用时(毫秒)',
    `score` int DEFAULT 0 COMMENT '得分',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_match` (`match_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对局参赛记录表';

-- ========================================
-- 验证更新
-- ========================================

-- 查看表结构
SHOW CREATE TABLE `competition`;
SHOW CREATE TABLE `competition_registration`;
SHOW CREATE TABLE `competition_match`;
SHOW CREATE TABLE `match_participation`;

-- 查看所有竞赛相关表
SHOW TABLES LIKE '%compet%';
SHOW TABLES LIKE '%match%';
SHOW TABLES LIKE '%team%';

-- 完成提示
SELECT '迁移完成！表结构已更新。' AS message;
