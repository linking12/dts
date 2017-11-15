CREATE TABLE `dts_branch_error_log` (
  `branch_id` bigint(20) NOT NULL,
  `tx_id` bigint(20) NOT NULL,
  `client_ip` varchar(15) COLLATE utf8_bin NOT NULL,
  `client_info` varchar(255) COLLATE utf8_bin NOT NULL,
  `state` tinyint(1) NOT NULL DEFAULT '0',
  `gmt_created` datetime NOT NULL,
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_notify` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`branch_id`),
  UNIQUE KEY `branch_id` (`branch_id`),
  KEY `tx_id` (`tx_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `dts_branch_log` (
  `branch_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tx_id` bigint(20) NOT NULL,
  `client_ip` varchar(200) COLLATE utf8_bin NOT NULL,
  `client_info` varchar(200) COLLATE utf8_bin NOT NULL,
  `state` tinyint(1) NOT NULL,
  `gmt_created` datetime NOT NULL,
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`branch_id`),
  KEY `tx_id` (`tx_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `dts_global_log` (
  `tx_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `state` tinyint(1) NOT NULL,
  `gmt_created` datetime NOT NULL,
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`tx_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
