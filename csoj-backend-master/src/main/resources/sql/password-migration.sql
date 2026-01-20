-- ============================================================
-- CSOJ 密码迁移脚本：MD5 -> BCrypt
-- ============================================================
-- 说明：
-- 1. 此脚本用于将现有 MD5 密码迁移到 BCrypt
-- 2. 迁移采用渐进式策略：用户登录时自动升级
-- 3. 无需一次性迁移所有密码
-- ============================================================

-- 方案说明：
-- 由于 BCrypt 哈希值（60字符）与 MD5（32字符）长度不同，
-- 我们采用渐进式迁移方案：
--
-- 1. 系统支持同时识别 BCrypt 和 MD5 密码
-- 2. 用户登录时，如果检测到 MD5 密码：
--    - 验证 MD5 密码
--    - 验证成功后自动升级为 BCrypt
--    - 更新数据库
-- 3. 新注册用户直接使用 BCrypt
--
-- 优点：
-- - 无需停机维护
-- - 无需批量迁移
-- - 用户无感知
-- - 降低风险
-- ============================================================

-- 查看当前密码加密方式分布
SELECT
    LENGTH(userPassword) AS password_length,
    CASE
        WHEN LENGTH(userPassword) = 60 AND userPassword LIKE '$2$%' THEN 'BCrypt (old)'
        WHEN LENGTH(userPassword) = 60 AND (userPassword LIKE '$2a$%' OR userPassword LIKE '$2b$%') THEN 'BCrypt (new)'
        WHEN LENGTH(userPassword) = 32 THEN 'MD5'
        ELSE 'Unknown'
    END AS password_type,
    COUNT(*) AS user_count
FROM user
GROUP BY LENGTH(userPassword), userPassword
ORDER BY user_count DESC;

-- ============================================================
-- 如果需要强制批量迁移（不推荐，仅用于紧急情况）
-- ============================================================

-- 注意：批量迁移需要应用程序配合，因为 BCrypt 哈希值每次不同
-- 以下脚本仅供参考，实际迁移应在应用层进行

-- 创建临时表存储待迁移用户
-- CREATE TABLE user_password_migration (
--     id BIGINT PRIMARY KEY,
--     user_account VARCHAR(256) NOT NULL,
--     old_password VARCHAR(32) NOT NULL,
--     new_password VARCHAR(60),
--     migrated TINYINT(1) DEFAULT 0,
--     migrate_time DATETIME,
--     INDEX idx_migrated (migrated),
--     INDEX idx_account (user_account)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- -- 插入 MD5 用户
-- INSERT INTO user_password_migration (id, user_account, old_password)
-- SELECT id, userAccount, userPassword
-- FROM user
-- WHERE LENGTH(userPassword) = 32;
--
-- -- 注意：实际迁移需要应用层读取此表，使用 PasswordUtil.hash() 生成新密码后更新

-- ============================================================
-- 验证迁移结果
-- ============================================================

-- 迁移后查看各类型密码数量
SELECT
    CASE
        WHEN LENGTH(userPassword) = 60 AND (userPassword LIKE '$2a$%' OR userPassword LIKE '$2b$%') THEN 'BCrypt'
        WHEN LENGTH(userPassword) = 32 THEN 'MD5 (Not Migrated)'
        ELSE 'Unknown'
    END AS password_type,
    COUNT(*) AS user_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM user), 2) AS percentage
FROM user
GROUP BY
    CASE
        WHEN LENGTH(userPassword) = 60 AND (userPassword LIKE '$2a$%' OR userPassword LIKE '$2b$%') THEN 'BCrypt'
        WHEN LENGTH(userPassword) = 32 THEN 'MD5 (Not Migrated)'
        ELSE 'Unknown'
    END
ORDER BY user_count DESC;

-- ============================================================
-- 回滚方案（紧急情况使用）
-- ============================================================

-- 如果迁移出现问题，可以使用以下逻辑回滚：
-- 1. 恢复数据库备份
-- 2. 或使用 MD5 验证逻辑重新部署旧版本应用

-- ============================================================
-- 安全提示
-- ============================================================

-- 1. 执行任何数据库操作前务必备份
-- 2. 建议在测试环境先验证
-- 3. 渐进式迁移是最安全的方案
-- 4. 监控迁移进度和用户登录情况

-- 备份命令示例：
-- mysqldump -u root -p oj_db user > user_backup_$(date +%Y%m%d_%H%M%S).sql
