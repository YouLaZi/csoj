-- 比赛表
create table if not exists contest
(
    id                     bigint auto_increment comment 'id' primary key,
    title                  varchar(512)                       not null comment '比赛名称',
    description            text                               null comment '比赛描述',
    type                   varchar(32)   default 'PUBLIC'      not null comment '比赛类型（PUBLIC-公开赛，PRIVATE-私有赛，PASSWORD-密码赛）',
    password               varchar(128)                        null comment '比赛密码',
    questionIds            json                               null comment '关联的题目ID列表',
    startTime              datetime                           not null comment '开始时间',
    endTime                datetime                           not null comment '结束时间',
    userId                 bigint                             not null comment '创建用户ID（组织者）',
    status                 varchar(32)   default 'DRAFT'       not null comment '状态（DRAFT-草稿，ONGOING-进行中，ENDED-已结束）',
    participantCount       int          default 0             not null comment '参与人数',
    enableRanking          tinyint(1)   default 1             not null comment '是否启用排行榜',
    showRealTimeRanking    tinyint(1)   default 1             not null comment '是否显示实时排名',
    rankingFreezeMinutes   int          default 0             not null comment '封榜时间（分钟）',
    createTime             datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime             datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete               tinyint      default 0             not null comment '是否删除',
    index idx_userId (userId),
    index idx_status (status),
    index idx_startTime (startTime),
    index idx_endTime (endTime)
) comment '比赛' collate = utf8mb4_unicode_ci;

-- 比赛参与者表
create table if not exists contest_participant
(
    id              bigint auto_increment comment 'id' primary key,
    contestId       bigint                             not null comment '比赛ID',
    userId          bigint                             not null comment '用户ID',
    joinTime        datetime     default CURRENT_TIMESTAMP not null comment '参与时间',
    totalScore      int          default 0             not null comment '总得分',
    passedCount     int          default 0             not null comment '通过题目数',
    totalTime       bigint       default 0             not null comment '总用时（秒）',
    lastSubmitTime  bigint                             null comment '最后提交时间（时间戳）',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint      default 0             not null comment '是否删除',
    index idx_contestId (contestId),
    index idx_userId (userId),
    unique key uk_contest_user (contestId, userId)
) comment '比赛参与者' collate = utf8mb4_unicode_ci;
