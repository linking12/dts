SET NAMES utf8;
SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for txc_global_log
-- ----------------------------
CREATE TABLE `txc_global_log` (
  `tx_id` bigint(20) NOT NULL auto_increment,
  `state` tinyint(1) NOT NULL,
  `gmt_created` datetime NOT NULL,
  `gmt_modified` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mid` int(11) NOT NULL,
  PRIMARY KEY  (`tx_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
SET FOREIGN_KEY_CHECKS=1;

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for txc_branch_log
-- ----------------------------
CREATE TABLE `txc_branch_log` (
  `branch_id` bigint(20) NOT NULL auto_increment,
  `tx_id` bigint(20) NOT NULL,
  `client_app_name` varchar(50) collate utf8_bin NOT NULL,
  `client_ip` varchar(15) collate utf8_bin NOT NULL,
  `client_info` varchar(200) collate utf8_bin NOT NULL,
  `udata` varchar(200) collate utf8_bin default NULL,
  `commit_mode` tinyint(1) unsigned zerofill NOT NULL,
  `state` tinyint(1) NOT NULL,
  `gmt_created` datetime NOT NULL,
  `gmt_modified` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mid` int(11) NOT NULL,
  PRIMARY KEY  (`branch_id`),
  KEY `tx_id` (`tx_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
SET FOREIGN_KEY_CHECKS=1;

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for txc_branch_error_log
-- ----------------------------
CREATE TABLE `txc_branch_error_log` (
  `branch_id` bigint(20) NOT NULL,
  `tx_id` bigint(20) NOT NULL,
  `client_app_name` varchar(50) collate utf8_bin NOT NULL,
  `client_ip` varchar(15) collate utf8_bin NOT NULL,
  `client_info` varchar(255) collate utf8_bin NOT NULL,
  `commit_mode` tinyint(1) unsigned zerofill NOT NULL,
  `state` tinyint(1) NOT NULL default '0',
  `gmt_created` datetime NOT NULL,
  `gmt_modified` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `is_notify` tinyint(1) default '0',
  `rt_sql` blob,
  `mid` int(11) default NULL,
  PRIMARY KEY  (`branch_id`),
  UNIQUE KEY `branch_id` (`branch_id`),
  KEY `tx_id` (`tx_id`),
  KEY `client_app_name` USING BTREE (`client_app_name`),
  KEY `commit_mode` (`commit_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
SET FOREIGN_KEY_CHECKS=1;

