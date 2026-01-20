CREATE TABLE `tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name` varchar(255) NOT NULL COMMENT '标签名称',
    `color` varchar(255) DEFAULT NULL COMMENT '标签颜色',
    `type` int(11) DEFAULT NULL COMMENT '标签类型（0-题目标签，1-讨论区标签）',
    `userId` bigint(20) DEFAULT NULL COMMENT '创建用户 id',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签';