CREATE TABLE `dts_undo_log` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `xid` char(64) NOT NULL,
  `branch_id` bigint(20) NOT NULL,
  `rollback_info` longblob,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modified` datetime DEFAULT NULL,
  `status` int(10) DEFAULT NULL,
  `server` char(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `branch_id` (`branch_id`,`xid`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1;
