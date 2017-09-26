package io.dts.parser.vistor.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.dts.common.exception.DtsException;
import io.dts.parser.model.TxcColumnMeta;
import io.dts.parser.model.TxcIndex;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.vistor.support.ISQLStatement;
import io.dts.parser.vistor.support.PlaceHolderManager;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * @author xiaoyan
 */
public class TxcInsertVisitor extends TxcBaseVisitor {

	private static final Logger logger = LoggerFactory.getLogger(TxcInsertVisitor.class);


	public TxcInsertVisitor(Connection connection, ISQLStatement stmt) throws SQLException {
		super(connection, stmt);
	}

	@Override
	protected String parseUserSql() {
		return null;
	}

	@Override
	protected String parseUserSql0() {
		return null;
	}


	@Override
	public TxcTable executeAndGetFrontImage(Statement st) throws SQLException {
		// insert前不需进行任何操作
		TxcTableMeta tableMeta = getTableMeta();
		TxcTable tableOriginalValue = getTableOriginalValue();
		tableOriginalValue.setTableMeta(tableMeta);
		tableOriginalValue.setTableName(tableMeta.getTableName());
		tableOriginalValue.setAlias(tableMeta.getAlias());
		tableOriginalValue.setSchemaName(tableMeta.getSchemaName());
		return tableOriginalValue;
	}

	@Override
	public TxcTable executeAndGetRearImage(Statement st) throws SQLException {
		String sql = getSelectSql() + getWhereCondition(st);

		TxcTable tablePresentValue = getTablePresentValue();
		TxcTableMeta tableMeta = getTableMeta();

		Map<String/* 索引名 */, TxcIndex> allIndexes = tableMeta.getAllIndexes();
		for (Map.Entry<String, TxcIndex> entry : allIndexes.entrySet()) {
			logger.info(" [" + entry.getKey() + "--->" + entry.getValue() + "] ");
		}

		// SQL执行后查询DB行现值，用户脏读检查
		// 此时，还没有拿到数据的索引值，因此需要使用不带KEY的SQL去查询现值
		tablePresentValue.setTableMeta(tableMeta);
		tablePresentValue.setTableName(tableMeta.getTableName());
		tablePresentValue.setAlias(tableMeta.getAlias());
		tablePresentValue.setSchemaName(tableMeta.getSchemaName());
		tablePresentValue.setLines(addLines(sql));

		logger.info("tablePresentValue:" + tablePresentValue.getLinesNum());
		return tablePresentValue;
	}

	@Override
	public String parseSelectSql() {
		StringBuilder selectSqlAppender = new StringBuilder();
		selectSqlAppender.append("SELECT ");

		Insert statement = (Insert) getSQLStatement().getStatement();

		List<Column> columns = statement.getColumns();
		if (columns != null) {
			for (Column column : columns) {
				selectSqlAppender.append(column.getColumnName()).append(",");
			}
			selectSqlAppender.deleteCharAt(selectSqlAppender.length() - 1);
		}
		selectSqlAppender.append(getTableName());

		return selectSqlAppender.toString();
	}

	/**
	 * 根据insert是否显式指定主键。<br>
	 * 如果指定了主键 ： 以全部输入字段作为查询条件。<br>
	 * 如果没有指定主键：去DB中获取last_insert_key，如果存在则使用自增主键为查询条件，否则以全部字段为查询条件<br>
	 * 仅支持数字、字符串为查询条件，其他类型直接忽略掉，但是，如果其他类型被指定为主键，则抛异常。<br>
	 */
	@SuppressWarnings("serial")
	@Override
	public String parseWhereCondition(Statement st) {
		Insert insert = (Insert) getSQLStatement().getStatement();

		ItemsList itemsList = insert.getItemsList();
		if (!(itemsList instanceof ExpressionList)) {
			throw new DtsException("unsupport complex insert sql" + sql);
		}

		StringBuilder whereSqlAppender = getNullSqlAppenderBuilder();
		whereSqlAppender.append(" WHERE ");

		List<Column> columns = insert.getColumns();

		if (getTableMeta().isContainsPriKey(new ArrayList<String>() {
			{
				for (Column column : columns) {
					add(column.getColumnName());
				}
			}
		})) {
			// 指定了主键
			logger.info("has pk value");
			selectByPK(whereSqlAppender, (ExpressionList)itemsList, columns);
		} else {
			// 没有指定主键
			logger.info("no has pk value");
			selectByAutoIncreaseKey(whereSqlAppender, (ExpressionList)itemsList, columns, st);
		}

		logger.info("whereSqlAppender:" + whereSqlAppender.toString());
		return whereSqlAppender.toString();
	}

	private void selectByPK(StringBuilder appender, ExpressionList itemsList, List<Column> cols) {
		boolean bOrFlag = true;
		PlaceHolderManager phm = getPlaceHolderManager();
		// SQL中不含有占位符
		if (phm == null) {
			List<Expression> args = itemsList.getExpressions();
			for (int i = 0; i < args.size(); i++) {
				String attrName = cols.get(i).getColumnName();
				TxcTableMeta tableMeta = getTableMeta();
				Map<String, TxcColumnMeta> index = tableMeta.getPrimaryKeyMap();

				TxcColumnMeta colMeta = tableMeta.getAllColumns().get(attrName.toUpperCase());
				if (colMeta == null) {
					colMeta = tableMeta.getAllColumns().get(attrName.toLowerCase());
				}

				if (colMeta.isCommonType() == false) {
					continue;
				}

				String tableName = colMeta.getTableName();
				String colName = colMeta.getColumnName().toUpperCase();
				if (index.get(colName) == null) {
					continue;
				}

				Object value = null;
				Expression arg = args.get(i);
				if (arg instanceof LongValue) {
					LongValue pNumber = (LongValue) arg;
					value = pNumber.getValue();
				} else if (arg instanceof StringValue) {
					StringValue pString = (StringValue) arg;
					value = pString.getValue();
				} else if (arg instanceof DoubleValue) {
					DoubleValue pString = (DoubleValue) arg;
					value = pString.getValue();
				}

				attrName = String.format("%s.%s", tableName, attrName);
				TxcObjectWapper.appendParamMarkerObject(attrName, value, appender);
			}
		}
		// SQL中含占位符
		else {
			List<Expression> args = itemsList.getExpressions();
			for (int i = 0; i < args.size(); i++) {
				String attrName = cols.get(i).getColumnName();
				TxcTableMeta tableMeta = getTableMeta();
				Map<String, TxcColumnMeta> index = tableMeta.getPrimaryKeyMap();

				TxcColumnMeta colMeta = tableMeta.getAllColumns().get(attrName.toUpperCase());
				if (colMeta == null) {
					colMeta = tableMeta.getAllColumns().get(attrName.toLowerCase());
				}

				if (colMeta.isCommonType() == false) {
					continue;
				}

				String tableName = colMeta.getTableName();
				String colName = colMeta.getColumnName().toUpperCase();
				if (index.get(colName) == null) {
					continue;
				}

				Object value = null;
				Expression arg = args.get(i);
				if (arg instanceof LongValue) {
					LongValue pNumber = (LongValue) arg;
					value = pNumber.getValue();
				} else if (arg instanceof StringValue) {
					StringValue pString = (StringValue) arg;
					value = pString.getValue();
				} else if (arg instanceof DoubleValue) {
					DoubleValue pString = (DoubleValue) arg;
					value = pString.getValue();
				} else if (arg instanceof JdbcParameter) {
					JdbcParameter parameter = (JdbcParameter) arg;
					int pMarkerIndex = parameter.getIndex();
					if (phm != null) {
						value = phm.getPlaceHolder(pMarkerIndex).get(0);// insert语句的占位符可能有多行数据
					}
				}

				attrName = String.format("%s.%s", tableName, attrName);
				TxcObjectWapper.appendParamMarkerObject(attrName, value, appender);
			}
		}
	}

	private void selectByAutoIncreaseKey(StringBuilder appender, ExpressionList itemsList, List<Column> cols, Statement st) {
		TxcColumnMeta column = getTableMeta().getAutoIncreaseColumn();
		if (column == null) {
			throw new IllegalArgumentException("no auto increase column.");
		}

		try {
			ResultSet rs = null;
			try {
				rs = st.getGeneratedKeys();
			} catch (SQLException e) {
				// java.sql.SQLException: Generated keys not requested. You need
				// to
				// specify Statement.RETURN_GENERATED_KEYS to
				// Statement.executeUpdate() or Connection.prepareStatement().
				if (e.getSQLState().equalsIgnoreCase("S1009")) {
					rs = st.executeQuery("SELECT LAST_INSERT_ID()"); // 通过额外查询获取
				}
			}

			if (rs != null) {
				PlaceHolderManager phm = getPlaceHolderManager();
				if (phm != null && phm.getPlaceHolderNum() > 0) {
					boolean bOrFlag = true;
					if (rs.next()) {
						for (int i = 0; i < phm.getPlaceHolderLineNum(); i++) {
							if (bOrFlag) {
								bOrFlag = false;
							} else {
								appender.append(" OR ");
							}

							appender.append(column.getColumnName());
							appender.append("=");
							appender.append(rs.getObject(1));
							appender.append("+" + i);
						}
					}
				} else {
					boolean bOrFlag = true;
					while (rs.next()) {
						if (bOrFlag) {
							bOrFlag = false;
						} else {
							appender.append(" OR ");
						}

						appender.append(column.getColumnName());
						appender.append("=");
						appender.append(rs.getObject(1));
					}
				}
				rs.close();
			}
			// 如果获取不到自增Key值，则退化成按所有字段进行搜索
			else {
				selectByAllFields(appender, itemsList, cols);
			}
		} catch (SQLException e) {
			throw new DtsException(e);
		}
	}

	private void selectByAllFields(StringBuilder appender, ExpressionList rows, List<Column> cols) {
		boolean bOrFlag = true;
		PlaceHolderManager phm = getPlaceHolderManager();
		// SQL中不含有占位符
		if (phm == null) {
			if (rows == null || rows.getExpressions().isEmpty()) {
				return;
			}

			setRow(cols, rows, 0, appender);
		}
		// SQL中含占位符
		else {
			int lineNum = phm.getPlaceHolder(1).size();
			Expression row = rows.getExpressions().get(0);
			setRow(cols, rows, 0, appender);
		}
	}

	/**
	 * 分解数据库属性名和值，拼装where语句后的判断条件
	 *
	 * @param cols
	 *            属性名列表
	 * @param row
	 *            属性值组
	 * @param loop
	 *            使用占位符赋值的属性处于List中的位置
	 */
	private void setRow(List<Column> cols, ExpressionList row, int loop, StringBuilder appender) {
		boolean bFlag = true;
		List<Expression> args = row.getExpressions();

		for (int i = 0; i < args.size(); i++) {
			String attrName = cols.get(i).getColumnName();
			TxcTableMeta tableMeta = getTableMeta();
			TxcColumnMeta colMeta = tableMeta.getAllColumns().get(attrName);
			if (colMeta.isCommonType() == false) {
				continue;
			}

			Object value = null;
			Expression arg = args.get(i);
			PlaceHolderManager phm = getPlaceHolderManager();

			if (arg instanceof LongValue) {
				LongValue pNumber = (LongValue) arg;
				value = pNumber.getValue();
			} else if (arg instanceof StringValue) {
				StringValue pString = (StringValue) arg;
				value = pString.getValue();
			} else if (arg instanceof DoubleValue) {
				DoubleValue pString = (DoubleValue) arg;
				value = pString.getValue();
			} else if (arg instanceof JdbcParameter) {
				JdbcParameter parameter = (JdbcParameter) arg;
				int pMarkerIndex = parameter.getIndex();
				if (phm != null) {
					value = phm.getPlaceHolder(pMarkerIndex).get(0);// insert语句的占位符可能有多行数据
				}
			}

			if (value != null) {
				if (bFlag) {
					bFlag = false;
				} else {
					appender.append(" AND ");
				}
				TxcObjectWapper.appendParamMarkerObject(attrName, value, appender);
			}
		}
	}


	@Override
	public String getsql(final String extraWhereCondition) {
		return null;
	}




}
