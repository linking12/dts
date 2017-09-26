package io.dts.parser.vistor;

import java.sql.Connection;
import java.sql.SQLException;

import io.dts.parser.vistor.mysql.TxcBaseVisitor;
import io.dts.parser.vistor.mysql.TxcDeleteVisitor;
import io.dts.parser.vistor.mysql.TxcInsertVisitor;
import io.dts.parser.vistor.mysql.TxcSelectVisitor;
import io.dts.parser.vistor.mysql.TxcUpdateVisitor;
import io.dts.parser.vistor.support.DtsSQLStatement;
import io.dts.parser.vistor.support.ISQLStatement;
import net.sf.jsqlparser.JSQLParserException;

public class TxcVisitorFactory {

	/**
	 * 获取SQL解析器<br>
	 * 由各种数据源分别维护<br>
	 *
	 * @return
	 * @throws SQLException
	 */
	public static ITxcVisitor getSqlVisitor(Connection connection, final String sql) throws SQLException {
		TxcBaseVisitor visit = null;

      final ISQLStatement node;
      try {
        node = new DtsSQLStatement(sql);
      } catch (JSQLParserException e) {
        throw new SQLException("sql parse error", e);
      }
      switch (node.getType()) {
		case SELECT:
			visit = new TxcSelectVisitor(connection, node);
			break;
		case DELETE:
			visit = new TxcDeleteVisitor(connection, node);
			break;
		case INSERT:
			visit = new TxcInsertVisitor(connection, node);
			break;
		case UPDATE:
			visit = new TxcUpdateVisitor(connection, node);
			break;
		}

		return visit;
	}
}
