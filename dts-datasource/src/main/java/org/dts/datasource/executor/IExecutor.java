package org.dts.datasource.executor;

import java.sql.SQLException;

public interface IExecutor<T> {
	T update(Object... args) throws SQLException;

	T query(Object... args) throws SQLException;
}
