/*---- create table rt_sql---------*/
/*---- 单表总记录数:99999,单表日增记录数:999999 ----*/

create table rt_sql (
	id bigint unsigned  not null comment '主键' auto_increment,
	gmt_create datetime comment '创建时间',
	gmt_modified datetime comment '修改时间',
	xid varchar(20)  not null comment '事务ID',
	branch_id bigint unsigned  not null comment '分支ID',
	tsql blob  not null comment '重试Sql',
	primary key (id),
	key xid (xid,branch_id)
) engine=InnoDB default charset=utf8 comment='rt模式SQL存储表';

/*---- create table txc_activity_info---------*/
/*---- 单表总记录数:9999999,单表日增记录数:999999 ----*/

create table txc_activity_info (
	id bigint unsigned  not null comment '主键' auto_increment,
	gmt_create datetime  not null comment '创建时间',
	gmt_modified datetime  not null comment '修改时间',
	xid varchar(100)  not null comment '全局事务ID',
	branch_id int unsigned  not null comment '分支事务ID',
	table_name varchar(100)  not null comment '互斥锁表名',
	key_value bigint unsigned  not null comment '互斥锁主键值',
	primary key (id)
) engine=InnoDB default charset=utf8 comment='事务锁表';

/*---- create table txc_undo_log---------*/
/*---- 单表总记录数:999999,单表日增记录数:99999 ----*/

create table txc_undo_log (
	id bigint unsigned  not null comment '主键' auto_increment,
	gmt_create datetime  not null comment '创建时间',
	gmt_modified datetime  not null comment '修改时间',
	xid varchar(100)  not null comment '全局事务ID',
	branch_id int unsigned  not null comment '分支事务ID',
	rollback_info blob  not null comment 'LOG',
	status bigint unsigned  not null comment '状态',
	server varchar(32)  not null comment '分支所在DB IP',
	primary key (id),
	key unionkey (xid,branch_id)
) engine=InnoDB default charset=utf8 comment='事务日志表';