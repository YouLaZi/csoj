-- =====================================================
-- OJ系统交互性与可玩性增强 - 数据库表
-- 包含: 猫咪养成、成就系统、每日挑战、数据可视化
-- =====================================================

-- =====================================================
-- 1. 猫咪养成系统表
-- =====================================================

-- 用户猫咪表
CREATE TABLE IF NOT EXISTS `user_cat` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `catName` varchar(64) DEFAULT '小橘' COMMENT '猫咪名字',
    `level` int NOT NULL DEFAULT 1 COMMENT '等级 (1-100)',
    `experience` int NOT NULL DEFAULT 0 COMMENT '当前经验值',
    `hunger` int NOT NULL DEFAULT 80 COMMENT '饱食度 (0-100)',
    `happiness` int NOT NULL DEFAULT 80 COMMENT '快乐值 (0-100)',
    `health` int NOT NULL DEFAULT 100 COMMENT '健康值 (0-100)',
    `energy` int NOT NULL DEFAULT 100 COMMENT '精力值 (0-100)',
    `mood` varchar(32) DEFAULT 'happy' COMMENT '心情 (happy/sad/thinking/sleeping/excited/surprised)',
    `lastFeedTime` datetime NULL COMMENT '最后喂食时间',
    `lastPlayTime` datetime NULL COMMENT '最后玩耍时间',
    `lastSleepTime` datetime NULL COMMENT '最后睡眠时间',
    `totalFeedCount` int NOT NULL DEFAULT 0 COMMENT '总喂食次数',
    `totalPlayCount` int NOT NULL DEFAULT 0 COMMENT '总玩耍次数',
    `createDay` date NOT NULL COMMENT '猫咪创建日期',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户猫咪';

-- 猫咪物品表
CREATE TABLE IF NOT EXISTS `cat_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `itemCode` varchar(64) NOT NULL COMMENT '物品代码',
    `itemName` varchar(128) NOT NULL COMMENT '物品名称',
    `itemType` varchar(32) NOT NULL COMMENT '物品类型 (food/toy/accessory/decoration)',
    `description` varchar(512) NULL COMMENT '物品描述',
    `effect` json NULL COMMENT '物品效果JSON {"hunger": 20, "happiness": 10}',
    `price` int NOT NULL DEFAULT 0 COMMENT '价格(积分)',
    `icon` varchar(64) NULL COMMENT '图标emoji',
    `rarity` varchar(32) DEFAULT 'common' COMMENT '稀有度 (common/rare/epic/legendary)',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪物品';

-- 用户猫咪物品背包
CREATE TABLE IF NOT EXISTS `user_cat_inventory` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `itemCode` varchar(64) NOT NULL COMMENT '物品代码',
    `quantity` int NOT NULL DEFAULT 0 COMMENT '数量',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId_itemCode` (`userId`, `itemCode`),
    INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户猫咪物品背包';

-- 猫咪装扮表
CREATE TABLE IF NOT EXISTS `cat_decoration` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `decorationCode` varchar(64) NOT NULL COMMENT '装扮代码',
    `isActive` tinyint NOT NULL DEFAULT 0 COMMENT '是否当前使用',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪装扮';

-- =====================================================
-- 2. 成就系统表
-- =====================================================

-- 成就定义表
CREATE TABLE IF NOT EXISTS `achievement` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `achievementCode` varchar(64) NOT NULL COMMENT '成就代码',
    `achievementName` varchar(128) NOT NULL COMMENT '成就名称',
    `description` varchar(512) NULL COMMENT '成就描述',
    `category` varchar(32) NOT NULL COMMENT '成就分类 (problem/streak/social/special/cat)',
    `icon` varchar(64) NULL COMMENT '图标emoji',
    `gradient` varchar(256) NULL COMMENT '徽章渐变CSS',
    `requirement` int NOT NULL DEFAULT 1 COMMENT '达成条件数值',
    `points` int NOT NULL DEFAULT 0 COMMENT '奖励积分',
    `rarity` varchar(32) DEFAULT 'common' COMMENT '稀有度 (common/rare/epic/legendary)',
    `isHidden` tinyint NOT NULL DEFAULT 0 COMMENT '是否隐藏成就',
    `sortOrder` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_achievementCode` (`achievementCode`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就定义';

-- 用户成就表
CREATE TABLE IF NOT EXISTS `user_achievement` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `achievementCode` varchar(64) NOT NULL COMMENT '成就代码',
    `progress` int NOT NULL DEFAULT 0 COMMENT '当前进度',
    `isUnlocked` tinyint NOT NULL DEFAULT 0 COMMENT '是否解锁',
    `unlockedTime` datetime NULL COMMENT '解锁时间',
    `isNew` tinyint NOT NULL DEFAULT 1 COMMENT '是否新成就(未查看)',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId_achievementCode` (`userId`, `achievementCode`),
    INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就';

-- 成就通知队列表
CREATE TABLE IF NOT EXISTS `achievement_notification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `achievementCode` varchar(64) NOT NULL COMMENT '成就代码',
    `isShown` tinyint NOT NULL DEFAULT 0 COMMENT '是否已显示',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_isShown` (`isShown`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就通知队列';

-- =====================================================
-- 3. 每日挑战系统表
-- =====================================================

-- 每日挑战表
CREATE TABLE IF NOT EXISTS `daily_challenge` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `challengeDate` date NOT NULL COMMENT '挑战日期',
    `questionId` bigint NOT NULL COMMENT '题目ID',
    `challengeType` varchar(32) NOT NULL DEFAULT 'daily' COMMENT '挑战类型 (daily/weekly/special)',
    `bonusPoints` int NOT NULL DEFAULT 30 COMMENT '完成奖励积分',
    `difficulty` varchar(32) NULL COMMENT '建议难度',
    `description` varchar(512) NULL COMMENT '挑战描述',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_challengeDate_type` (`challengeDate`, `challengeType`),
    INDEX `idx_questionId` (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日挑战';

-- 用户挑战参与表
CREATE TABLE IF NOT EXISTS `user_challenge` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `challengeId` bigint NOT NULL COMMENT '挑战ID',
    `isCompleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否完成',
    `isPerfect` tinyint NOT NULL DEFAULT 0 COMMENT '是否完美通关(首次提交即通过)',
    `attemptCount` int NOT NULL DEFAULT 0 COMMENT '尝试次数',
    `pointsEarned` int NOT NULL DEFAULT 0 COMMENT '获得积分',
    `completedTime` datetime NULL COMMENT '完成时间',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId_challengeId` (`userId`, `challengeId`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_challengeId` (`challengeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户挑战参与';

-- 挑战连胜表
CREATE TABLE IF NOT EXISTS `challenge_streak` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `currentStreak` int NOT NULL DEFAULT 0 COMMENT '当前连胜',
    `maxStreak` int NOT NULL DEFAULT 0 COMMENT '最大连胜',
    `lastChallengeDate` date NULL COMMENT '最后挑战日期',
    `totalCompleted` int NOT NULL DEFAULT 0 COMMENT '总完成数',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挑战连胜';

-- =====================================================
-- 4. 数据可视化系统表
-- =====================================================

-- 刷题热力图表
CREATE TABLE IF NOT EXISTS `problem_heatmap` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `date` date NOT NULL COMMENT '日期',
    `solvedCount` int NOT NULL DEFAULT 0 COMMENT '解决问题数',
    `submitCount` int NOT NULL DEFAULT 0 COMMENT '提交次数',
    `timeSpent` int NOT NULL DEFAULT 0 COMMENT '花费时间(分钟)',
    `difficulty` json NULL COMMENT '难度分布JSON',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId_date` (`userId`, `date`),
    INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷题热力图';

-- 技能进度表
CREATE TABLE IF NOT EXISTS `skill_progress` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `skillCode` varchar(64) NOT NULL COMMENT '技能代码(标签)',
    `skillName` varchar(128) NOT NULL COMMENT '技能名称',
    `level` int NOT NULL DEFAULT 1 COMMENT '技能等级',
    `experience` int NOT NULL DEFAULT 0 COMMENT '经验值',
    `problemsSolved` int NOT NULL DEFAULT 0 COMMENT '解决问题数',
    `lastPracticeTime` datetime NULL COMMENT '最后练习时间',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId_skillCode` (`userId`, `skillCode`),
    INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能进度';

-- 用户统计缓存表
CREATE TABLE IF NOT EXISTS `user_stats_cache` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `totalSolved` int NOT NULL DEFAULT 0 COMMENT '总解决数',
    `totalSubmissions` int NOT NULL DEFAULT 0 COMMENT '总提交数',
    `acceptRate` decimal(5,2) DEFAULT 0.00 COMMENT '通过率',
    `currentStreak` int NOT NULL DEFAULT 0 COMMENT '当前连胜',
    `maxStreak` int NOT NULL DEFAULT 0 COMMENT '最大连胜',
    `avgTimePerProblem` int DEFAULT 0 COMMENT '平均每题时间(分钟)',
    `skillScores` json NULL COMMENT '技能分数JSON {"dp": 80, "array": 90}',
    `weeklyStats` json NULL COMMENT '周统计JSON',
    `monthlyStats` json NULL COMMENT '月统计JSON',
    `lastUpdated` datetime NOT NULL COMMENT '最后更新时间',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计缓存';

-- =====================================================
-- 5. 初始化数据
-- =====================================================

-- 初始化猫咪物品
INSERT INTO `cat_item` (`itemCode`, `itemName`, `itemType`, `description`, `effect`, `price`, `icon`, `rarity`) VALUES
-- 食物
('cat_food_basic', '猫粮', 'food', '普通的猫粮，能填饱肚子', '{"hunger": 20}', 5, '🍚', 'common'),
('cat_food_fish', '小鱼干', 'food', '美味的小鱼干，猫咪最爱', '{"hunger": 35, "happiness": 10}', 15, '🐟', 'common'),
('cat_food_can', '猫罐头', 'food', '高级猫罐头，营养美味', '{"hunger": 50, "happiness": 20, "health": 5}', 30, '🥫', 'rare'),
('cat_food_premium', '三文鱼', 'food', '新鲜三文鱼，顶级美食', '{"hunger": 80, "happiness": 40, "health": 10}', 80, '🍣', 'epic'),
('cat_food_legendary', '金枪鱼盛宴', 'food', '传说中的金枪鱼盛宴', '{"hunger": 100, "happiness": 60, "health": 20}', 200, '👑', 'legendary'),
-- 玩具
('cat_toy_ball', '毛线球', 'toy', '经典的毛线球', '{"happiness": 15, "energy": -10}', 10, '🧶', 'common'),
('cat_toy_mouse', '玩具老鼠', 'toy', '会动的玩具老鼠', '{"happiness": 25, "energy": -15}', 25, '🐭', 'common'),
('cat_toy_laser', '激光笔', 'toy', '猫咪无法抗拒的红点', '{"happiness": 40, "energy": -20}', 50, '🔴', 'rare'),
('cat_toy_tower', '猫爬架', 'toy', '豪华猫爬架', '{"happiness": 60, "energy": -10}', 100, '🏠', 'epic'),
-- 配饰
('cat_acc_bow', '蝴蝶结', 'accessory', '可爱的蝴蝶结', '{"happiness": 5}', 20, '🎀', 'common'),
('cat_acc_hat', '小帽子', 'accessory', '帅气的小帽子', '{"happiness": 10}', 40, '🎩', 'rare'),
('cat_acc_crown', '皇冠', 'accessory', '王者之冠', '{"happiness": 30}', 150, '👑', 'legendary')
ON DUPLICATE KEY UPDATE `itemName` = VALUES(`itemName`);

-- 初始化成就定义
INSERT INTO `achievement` (`achievementCode`, `achievementName`, `description`, `category`, `icon`, `gradient`, `requirement`, `points`, `rarity`, `sortOrder`) VALUES
-- 刷题成就
('first_submit', '初来乍到', '提交第一份代码', 'problem', '📝', 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 1, 5, 'common', 1),
('first_accept', '一击即中', '首次通过题目', 'problem', '🎯', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 1, 10, 'common', 2),
('solve_10', '初露锋芒', '累计解决10道题目', 'problem', '⭐', 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', 10, 20, 'common', 3),
('solve_50', '小有成就', '累计解决50道题目', 'problem', '🌟', 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', 50, 50, 'rare', 4),
('solve_100', '百题斩将', '累计解决100道题目', 'problem', '🔥', 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', 100, 100, 'rare', 5),
('solve_500', '算法大师', '累计解决500道题目', 'problem', '👑', 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)', 500, 500, 'legendary', 6),
('solve_1000', '传说解题者', '累计解决1000道题目', 'problem', '🏆', 'linear-gradient(135deg, #ffd700 0%, #ff8c00 100%)', 1000, 1000, 'legendary', 7),
-- 签到成就
('checkin_first', '初次签到', '完成首次签到', 'streak', '📅', 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 1, 5, 'common', 20),
('checkin_7', '坚持不懈', '连续签到7天', 'streak', '💪', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 7, 30, 'common', 21),
('checkin_30', '习惯养成', '连续签到30天', 'streak', '🎖️', 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', 30, 100, 'rare', 22),
('checkin_100', '持之以恒', '连续签到100天', 'streak', '🏅', 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', 100, 300, 'epic', 23),
('checkin_365', '年度坚持', '连续签到365天', 'streak', '💎', 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)', 365, 1000, 'legendary', 24),
-- 社交成就
('first_post', '初发声', '发布第一篇帖子', 'social', '📢', 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 1, 10, 'common', 40),
('post_10', '活跃发言', '发布10篇帖子', 'social', '💬', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 10, 30, 'common', 41),
('solution_5', '热心助人', '发布5篇题解', 'social', '🤝', 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', 5, 50, 'rare', 42),
('solution_20', '题解达人', '发布20篇题解', 'social', '📚', 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', 20, 150, 'epic', 43),
-- 特殊成就
('perfect_score', '完美答卷', '首次提交即通过所有测试用例', 'special', '💯', 'linear-gradient(135deg, #ffd700 0%, #ff8c00 100%)', 1, 30, 'rare', 60),
('night_owl', '夜猫子', '在午夜(0:00-6:00)刷题', 'special', '🦉', 'linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%)', 1, 10, 'common', 61),
('early_bird', '早起鸟', '在早晨(6:00-8:00)刷题', 'special', '🐦', 'linear-gradient(135deg, #f5af19 0%, #f12711 100%)', 1, 10, 'common', 62),
('weekend_warrior', '周末战士', '在周末完成5道题目', 'special', '⚔️', 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)', 5, 20, 'common', 63),
('hard_master', '困难征服者', '完成10道困难题目', 'special', '🏔️', 'linear-gradient(135deg, #c31432 0%, #240b36 100%)', 10, 100, 'epic', 64),
-- 猫咪成就
('cat_owner', '铲屎官', '获得自己的猫咪', 'cat', '🐱', 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)', 1, 10, 'common', 80),
('cat_feed_50', '爱心喂养', '喂食猫咪50次', 'cat', '🍖', 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)', 50, 30, 'common', 81),
('cat_play_50', '玩伴', '与猫咪玩耍50次', 'cat', '🎾', 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)', 50, 30, 'common', 82),
('cat_level_10', '猫咪训练师', '将猫咪培养到10级', 'cat', '⭐', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 10, 50, 'rare', 83),
('cat_level_50', '资深铲屎官', '将猫咪培养到50级', 'cat', '🌟', 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', 50, 200, 'epic', 84)
ON DUPLICATE KEY UPDATE `achievementName` = VALUES(`achievementName`);
