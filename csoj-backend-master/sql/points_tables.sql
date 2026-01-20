# 积分系统相关表

use oj_db;

-- 用户积分表
create table if not exists user_points
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '用户id',
    totalPoints int      default 0                 not null comment '总积分',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '用户积分' collate = utf8mb4_unicode_ci;

-- 积分记录表
create table if not exists points_record
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '用户id',
    points     int                                not null comment '积分变动值',
    description varchar(256)                      not null comment '积分变动描述',
    type       varchar(128)                       not null comment '积分变动类型（签到、提交题目、题目通过、发布题解等）',
    relatedId  bigint                             null comment '相关联的id（如题目id、帖子id等）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_type (type)
) comment '积分记录' collate = utf8mb4_unicode_ci;

-- 用户签到表
create table if not exists user_checkin
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '用户id',
    checkinDate date                              not null comment '签到日期',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_checkinDate (checkinDate),
    unique uk_userId_checkinDate (userId, checkinDate)
) comment '用户签到' collate = utf8mb4_unicode_ci;