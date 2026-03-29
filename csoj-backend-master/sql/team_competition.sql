-- 团队竞赛系统数据库表
-- 创建时间: 2026-03-02
-- 版本: 2.0
--
-- 说明:
--   - 此文件用于创建全新的表结构
--   - 如果表已存在且需要更新，请使用 migrate_competition_v2.sql
--   - 此脚本使用 CREATE TABLE IF NOT EXISTS，不会覆盖现有表

-- 切换库
use oj_db;

-- ========================================
-- 1. 团队表
-- ========================================
CREATE TABLE IF NOT EXISTS `team` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '队伍ID',
    `name` varchar(100) NOT NULL COMMENT '队伍名称',
    `description` text COMMENT '队伍简介',
    `avatar` varchar(512) COMMENT '队伍头像URL',
    `max_members` int DEFAULT 5 COMMENT '最大成员数',
    `is_public` tinyint DEFAULT 1 COMMENT '是否公开招募',
    `invite_code` varchar(32) COMMENT '邀请码',
    `leader_id` bigint NOT NULL COMMENT '队长用户ID',
    `total_score` bigint DEFAULT 0 COMMENT '团队总积分',
    `win_count` int DEFAULT 0 COMMENT '胜场数',
    `lose_count` int DEFAULT 0 COMMENT '负场数',
    `draw_count` int DEFAULT 0 COMMENT '平局数',
    `rating` int DEFAULT 1000 COMMENT 'ELO积分',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_name` (`name`),
    INDEX `idx_leader` (`leader_id`),
    INDEX `idx_rating` (`rating`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- ========================================
-- 2. 团队成员表
-- ========================================
CREATE TABLE IF NOT EXISTS `team_member` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id` bigint NOT NULL COMMENT '团队ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role` varchar(20) DEFAULT 'member' COMMENT '角色：leader/vice_leader/member',
    `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `contribution_score` int DEFAULT 0 COMMENT '贡献积分',
    ` solved_count` int DEFAULT 0 COMMENT '解题数',
    `contest_count` int DEFAULT 0 COMMENT '参赛次数',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_team_user` (`team_id`, `user_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- ========================================
-- 3. 竞赛表
-- ========================================
CREATE TABLE IF NOT EXISTS `competition` (
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

-- ========================================
-- 4. 竞赛报名表
-- ========================================
CREATE TABLE IF NOT EXISTS `competition_registration` (
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

-- ========================================
-- 5. 竞赛对局表
-- ========================================
CREATE TABLE IF NOT EXISTS `competition_match` (
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

-- ========================================
-- 6. 对局参赛记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `match_participation` (
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
-- 7. 团队邀请记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `team_invitation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id` bigint NOT NULL COMMENT '团队ID',
    `inviter_id` bigint NOT NULL COMMENT '邀请人ID',
    `invitee_id` bigint COMMENT '被邀请人ID',
    `invite_code` varchar(32) NOT NULL COMMENT '邀请码',
    `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/accepted/rejected/expired',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_team` (`team_id`),
    INDEX `idx_invitee` (`invitee_id`),
    INDEX `idx_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队邀请记录表';

-- ========================================
-- 初始化测试数据
-- ========================================

-- 插入测试团队
-- INSERT INTO `team` (`name`, `description`, `leader_id`, `max_members`, `is_public`, `invite_code`)
-- VALUES ('测试团队A', '这是一个测试团队', 1, 5, 1, 'ABC123');

-- 插入测试竞赛
-- INSERT INTO `competition` (`name`, `description`, `type`, `status`, `max_teams`, `min_team_size`, `max_team_size`, `start_time`, `end_time`, `register_start_time`, `register_end_time`, `creator_id`)
-- VALUES ('CSOJ 春季团队赛', '第一届CSOJ春季团队编程竞赛', 'elimination', 0, 16, 1, 5, '2026-04-01 14:00:00', '2026-04-01 18:00:00', '2026-03-15 00:00:00', '2026-03-31 23:59:59', 1);
