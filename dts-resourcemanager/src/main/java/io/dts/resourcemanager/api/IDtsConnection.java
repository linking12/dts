package io.dts.resourcemanager.api;

import java.sql.Connection;
import java.sql.SQLException;

import io.dts.parser.struct.TxcRuntimeContext;

public interface IDtsConnection extends Connection {
	/**
	 * 获取不带事务的数据库连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	Connection getRawConnection() throws SQLException;

	/**
	 * 获取TxcDataSource
	 * 
	 * @return
	 * @throws SQLException
	 */
	IDtsDataSource getDataSource() throws SQLException;

	/**
	 * 获取上下文
	 * @return
	 */
	TxcRuntimeContext getTxcContext();

}
