-- 作业表
create table if not exists assignment
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(512)                       not null comment '作业标题',
    description text                               null comment '作业描述',
    type        varchar(32)   default 'PRACTICE'    not null comment '作业类型（PRACTICE-练习题，EXAM-考试题）',
    questionIds json                               null comment '关联的题目ID列表',
    difficulty  varchar(32)   default 'MEDIUM'      null comment '难度等级（EASY-简单，MEDIUM-中等，HARD-困难）',
    totalScore  int          default 100            not null comment '总分',
    passScore   int          default 60            not null comment '及格分数',
    deadline    datetime                            null comment '截止时间',
    userId      bigint                              not null comment '创建用户ID（教师）',
    classId     varchar(128)                        null comment '班级ID',
    isPublic    tinyint(1)   default 0             not null comment '是否公开',
    status      varchar(32)   default 'DRAFT'       not null comment '状态（DRAFT-草稿，PUBLISHED-已发布，CLOSED-已关闭）',
    submitCount int          default 0             not null comment '提交数量',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0             not null comment '是否删除',
    index idx_userId (userId),
    index idx_status (status),
    index idx_classId (classId),
    index idx_createTime (createTime)
) comment '作业' collate = utf8mb4_unicode_ci;

-- 作业提交表
create table if not exists assignment_submit
(
    id           bigint auto_increment comment 'id' primary key,
    assignmentId bigint                             not null comment '作业ID',
    userId       bigint                             not null comment '学生ID',
    questionIds  json                               null comment '提交的题目ID列表',
    scores       json                               null comment '各题目得分',
    totalScore   int          default 0             not null comment '总得分',
    status       varchar(32)   default 'SUBMITTED'   not null comment '提交状态（SUBMITTED-已提交，GRADED-已批改）',
    comment      text                               null comment '批改评语',
    gradedBy     bigint                             null comment '批改教师ID',
    gradedTime   datetime                           null comment '批改时间',
    submitTime   datetime     default CURRENT_TIMESTAMP not null comment '提交时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0             not null comment '是否删除',
    index idx_assignmentId (assignmentId),
    index idx_userId (userId),
    index idx_status (status),
    unique key uk_assignment_user (assignmentId, userId)
) comment '作业提交' collate = utf8mb4_unicode_ci;
