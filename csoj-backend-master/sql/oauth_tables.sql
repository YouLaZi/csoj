-- 第三方账号绑定表
-- 用于存储用户与第三方OAuth账号的绑定关系
-- 支持平台：GitHub、Gitee、QQ、微信

CREATE TABLE IF NOT EXISTS `user_oauth` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `userId` BIGINT NOT NULL COMMENT '关联的用户ID',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台类型: GITHUB/GITEE/QQ/WECHAT',
    `platformUserId` VARCHAR(128) NOT NULL COMMENT '第三方平台的用户ID',
    `openId` VARCHAR(128) NULL COMMENT '平台openId(QQ/微信)',
    `unionId` VARCHAR(128) NULL COMMENT '统一ID(微信开放平台unionId)',
    `nickname` VARCHAR(256) NULL COMMENT '第三方昵称',
    `avatar` VARCHAR(1024) NULL COMMENT '第三方头像URL',
    `accessToken` VARCHAR(512) NULL COMMENT '访问令牌(可选，用于API调用)',
    `bindTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_platform_user` (`platform`, `platformUserId`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_unionId` (`unionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方账号绑定表';
