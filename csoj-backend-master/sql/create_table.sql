# 数据库初始化

-- 创建库
create database if not exists oj_db;

-- 切换库
use oj_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    email        varchar(255)                           null comment '邮箱',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text                               null comment '题目答案',
    difficulty  varchar(255)                       null comment '难度',
    submitNum   int      default 0                 not null comment '题目提交数',
    acceptedNum int      default 0                 not null comment '题目通过数',
    judgeCase   text                               null comment '判题用例（json 数组）',
    judgeConfig text                               null comment '判题配置（json 对象）',
    thumbNum    int      default 0                 not null comment '点赞数',
    favourNum   int      default 0                 not null comment '收藏数',
    userId      bigint                             not null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '题目' collate = utf8mb4_unicode_ci;

-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交';

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- 评论表
create table if not exists comment
(
    id           bigint auto_increment comment 'id' primary key,
    content      text                               not null comment '评论内容',
    userId       bigint                             not null comment '评论创建用户 id',
    objectId     bigint                             not null comment '被评论对象 id（题目 id 或帖子 id）',
    objectType   varchar(128)                       not null comment '被评论对象类型（question/post）',
    parentId     bigint       default 0             null comment '父评论 id，为 0 则表示一级评论',
    rootId       bigint       default 0             null comment '根评论 id，为 0 则表示一级评论',
    replyUserId  bigint                             null comment '回复用户 id',
    likeCount    int          default 0             not null comment '点赞数',
    status       int          default 0             not null comment '评论状态（0-正常，1-被删除）',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0             not null comment '是否删除',
    index idx_objectId (objectId),
    index idx_userId (userId),
    index idx_parentId (parentId)
) comment '评论表' collate = utf8mb4_unicode_ci;

-- 评论点赞表
create table if not exists comment_like
(
    id         bigint auto_increment comment 'id' primary key,
    commentId  bigint                             not null comment '评论 id',
    userId     bigint                             not null comment '点赞用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_commentId (commentId),
    index idx_userId (userId)
) comment '评论点赞表';

CREATE TABLE if not exists `announcement` (
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

-- 学习进度表
CREATE TABLE IF NOT EXISTS `learning_progress` (
                                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                                   `solved_problems` int NOT NULL DEFAULT 0 COMMENT '已解决题目数量',
                                                   `recent_topics` json NULL COMMENT '最近学习的主题（JSON格式存储）',
                                                   `recommended_topics` json NULL COMMENT '推荐学习的主题（JSON格式存储）',
                                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                   `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
                                                   PRIMARY KEY (`id`),
                                                   UNIQUE INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表';

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

CREATE TABLE if not exists `tag` (
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
