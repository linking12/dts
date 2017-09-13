package org.dts.datasource.executor;

import java.sql.SQLException;

public interface IAtExcutor<T> extends IExecutor<T> {
	/**
	 * 关闭事务连接，暂无有效逻辑，仅记Log
	 * 
	 * @throws SQLException
	 */
	void txcClose() throws SQLException;

	/**
	 * 事务回滚逻辑
	 * 
	 * @param e
	 * @throws SQLException
	 */
	void txcRollback() throws SQLException;

	/**
	 * 事务提交逻辑
	 * 
	 * @throws SQLException
	 */
	void txcCommit() throws SQLException;
}
