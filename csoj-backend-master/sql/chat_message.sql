-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NULL COMMENT '问题ID（可选）',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` varchar(10) NOT NULL COMMENT '消息类型（user-用户消息，bot-机器人回复）',
  `content_type` varchar(10) NOT NULL DEFAULT 'text' COMMENT '内容类型（text-文本，code-代码，math-数学公式）',
  `language` varchar(20) NULL COMMENT '编程语言（当contentType为code时有效）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';