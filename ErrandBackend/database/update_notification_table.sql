-- 更新通知表结构，使其与实体类匹配
USE campus_errand;

-- 添加缺少的字段
alter table notifications 
add column tag_name varchar(100) comment '通知标签',
add column task_id bigint comment '关联任务ID',
add column notice_id bigint comment '关联公告ID',
add column read_at datetime comment '已读时间',
-- 修改字段名以匹配实体类
change column is_read `read` tinyint not null default 0 comment '是否已读：0-否 1-是',
change column create_time created_at datetime not null default CURRENT_TIMESTAMP comment '创建时间',
-- 删除不需要的字段
drop column related_id;

-- 更新索引
drop index if exists idx_is_read;
create index idx_read on notifications(`read`);

-- 更新现有数据
update notifications set `read` = is_read where is_read is not null;
