CREATE TABLE `announcement` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `title` varchar(255) NOT NULL COMMENT '标题',
    `content` text NOT NULL COMMENT '内容',
    `type` int(11) DEFAULT NULL COMMENT '公告类型（0-普通公告，1-重要公告，2-紧急公告）',
    `status` int(11) DEFAULT NULL COMMENT '状态（0-未发布，1-已发布）',
    `userId` bigint(20) DEFAULT NULL COMMENT '创建用户 id',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告';