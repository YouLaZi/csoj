-- 数据库索引优化
-- 执行前请先备份数据库
-- 创建时间: 2026-03-02

-- 切换库
use oj_db;

-- ========================================
-- 题目提交表优化
-- ========================================

-- 1. 添加状态索引（用于查询待判题/判题中的提交）
-- 适用于：SELECT * FROM question_submit WHERE status = 0
CREATE INDEX IF NOT EXISTS idx_status ON question_submit(status);

-- 2. 添加复合索引（用于按题目查询用户的提交记录）
-- 适用于：SELECT * FROM question_submit WHERE questionId = ? AND userId = ?
CREATE INDEX IF NOT EXISTS idx_questionId_userId ON question_submit(questionId, userId);

-- 3. 添加创建时间索引（用于按时间排序查询）
-- 适用于：SELECT * FROM question_submit ORDER BY createTime DESC
CREATE INDEX IF NOT EXISTS idx_createTime ON question_submit(createTime);

-- 4. 添加复合索引（用于查询用户在某个题目下的提交状态）
-- 适用于：统计用户在某题目的通过/失败情况
CREATE INDEX IF NOT EXISTS idx_userId_questionId_status ON question_submit(userId, questionId, status);

-- ========================================
-- 题目表优化
-- ========================================

-- 5. 添加创建时间索引（用于按时间排序）
-- 适用于：SELECT * FROM question ORDER BY createTime DESC
CREATE INDEX IF NOT EXISTS idx_createTime ON question(createTime);

-- 6. 添加难度索引（用于按难度筛选）
-- 适用于：SELECT * FROM question WHERE difficulty = '简单'
CREATE INDEX IF NOT EXISTS idx_difficulty ON question(difficulty);

-- 7. 添加复合索引（用于分页查询和排序）
-- 适用于：SELECT * FROM question ORDER BY createTime DESC LIMIT ?
CREATE INDEX IF NOT EXISTS idx_createTime_id ON question(createTime, id);

-- ========================================
-- 帖子表优化
-- ========================================

-- 8. 添加创建时间索引（用于按时间排序）
CREATE INDEX IF NOT EXISTS idx_createTime ON post(createTime);

-- 9. 添加复合索引（用于帖子列表查询）
CREATE INDEX IF NOT EXISTS idx_createTime_id ON post(createTime, id);

-- ========================================
-- 用户表优化
-- ========================================

-- 10. 添加用户账号唯一索引（确保账号唯一）
CREATE UNIQUE INDEX IF NOT EXISTS uk_userAccount ON user(userAccount);

-- 11. 添加用户角色索引（用于按角色筛选用户）
CREATE INDEX IF NOT EXISTS idx_userRole ON user(userRole);

-- ========================================
-- 积分记录表优化
-- ========================================

-- 12. 添加创建时间索引（用于按时间查询积分记录）
CREATE INDEX IF NOT EXISTS idx_createTime ON points_record(createTime);

-- 13. 添加复合索引（用于查询用户某类型的积分记录）
CREATE INDEX IF NOT EXISTS idx_userId_type_createTime ON points_record(userId, type, createTime);

-- ========================================
-- 评论表优化
-- ========================================

-- 14. 添加复合索引（用于查询某对象的评论列表）
CREATE INDEX IF NOT EXISTS idx_objectId_objectType_createTime ON comment(objectId, objectType, createTime);

-- ========================================
-- 公告表优化
-- ========================================

-- 15. 添加状态和类型索引
CREATE INDEX IF NOT EXISTS idx_status_type ON announcement(status, type);

-- ========================================
-- 分析表（查看索引使用情况）
-- ========================================

-- 查看题目提交表的索引
SHOW INDEX FROM question_submit;

-- 查看题目表的索引
SHOW INDEX FROM question;

-- 查看帖子表的索引
SHOW INDEX FROM post;

-- ========================================
-- 索引使用建议
-- ========================================

/*
1. 定期分析表以更新统计信息：
   ANALYZE TABLE question_submit;
   ANALYZE TABLE question;
   ANALYZE TABLE post;

2. 使用 EXPLAIN 分析查询计划：
   EXPLAIN SELECT * FROM question_submit WHERE status = 0;

3. 监控慢查询日志：
   SET GLOBAL slow_query_log = 'ON';
   SET GLOBAL long_query_time = 2;

4. 定期优化表：
   OPTIMIZE TABLE question_submit;
   OPTIMIZE TABLE question;
*/
