package io.dts.parser.vistor.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import io.dts.parser.model.TxcTable;
import io.dts.parser.vistor.support.ISQLStatement;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class TxcSelectVisitor extends TxcBaseVisitor {


	public TxcSelectVisitor(Connection connection, ISQLStatement stmt) throws SQLException {
		super(connection, stmt);
	}

	@Override
	public TxcTable executeAndGetFrontImage(final Statement st) throws SQLException {
		return getTableOriginalValue();
	}

	@Override
	public TxcTable executeAndGetRearImage( final Statement st) throws SQLException {
		return getTablePresentValue();
	}

	@Override
	public String getsql(final String extraWhereCondition) {
		return null;
	}

	@Override
	protected String parseSelectSql() {
		PlainSelect ps = getPlainSelect();

		StringBuilder selectSb = new StringBuilder("select ");

		for (SelectItem selectItem : ps.getSelectItems()) {
			selectSb.append(selectItem.toString()).append(",");
		}
		System.out.println(selectSb);
		selectSb.deleteCharAt(selectSb.length()-1);

		selectSb.append(" from ");
		selectSb.append(ps.getFromItem().toString());
		List<Join> joins = ps.getJoins();
		if (joins != null) {
			for (Join join : joins) {
				if (join.isSimple()) {
					selectSb.append(",");
				} else {
					selectSb.append(" ");
				}
				selectSb.append(join.toString());
			}
		}
		return selectSb.toString();
	}

	private PlainSelect getPlainSelect() {
		return ((PlainSelect) ((Select) getSQLStatement().getStatement()).getSelectBody());
	}

	@Override
	protected String parseWhereCondition(final Statement st) {
		Expression where = getPlainSelect().getWhere();
		return where != null ?  where.toString() : null;
	}
}
