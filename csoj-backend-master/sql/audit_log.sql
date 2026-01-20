-- 操作审计日志表
create table if not exists audit_log
(
    id             bigint auto_increment comment 'id' primary key,
    user_id        bigint                               null comment '操作用户ID',
    user_account   varchar(256)                         null comment '操作用户账号',
    user_name      varchar(256)                         null comment '操作用户姓名',
    module         varchar(128)                         null comment '操作模块',
    operation_type varchar(64)                          null comment '操作类型（LOGIN/LOGOUT/CREATE/UPDATE/DELETE/QUERY/EXPORT/IMPORT等）',
    description    varchar(512)                         null comment '操作描述',
    method         varchar(512)                         null comment '请求方法',
    params         text                                 null comment '请求参数',
    result         mediumtext                           null comment '返回结果',
    duration       bigint                               null comment '执行时长（毫秒）',
    status         varchar(32)                          null comment '操作状态（SUCCESS/FAILURE）',
    error_msg      varchar(2048)                        null comment '错误信息',
    client_ip      varchar(64)                          null comment '客户端IP',
    user_agent     varchar(512)                         null comment '用户代理',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    is_delete      tinyint      default 0                 not null comment '是否删除',
    index idx_user_id (user_id),
    index idx_module (module),
    index idx_operation_type (operation_type),
    index idx_status (status),
    index idx_create_time (create_time),
    index idx_client_ip (client_ip)
) comment '操作审计日志' collate = utf8mb4_unicode_ci;
