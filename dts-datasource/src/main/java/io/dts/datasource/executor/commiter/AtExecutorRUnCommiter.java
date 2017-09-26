package io.dts.datasource.executor.commiter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.datasource.executor.BaseStatementUnit;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcTable;
import io.dts.parser.vistor.ITxcVisitor;
import io.dts.parser.vistor.TxcVisitorFactory;
import io.dts.parser.vistor.mysql.TxcDeleteVisitor;

public class AtExecutorRUnCommiter {

  private static final Logger logger = LoggerFactory.getLogger(AtExecutorRUnCommiter.class);

  private ITxcVisitor txcVisitor;

  private BaseStatementUnit baseStatementUnit;

  private List<List<Object>> parameterSets;

  public AtExecutorRUnCommiter(BaseStatementUnit baseStatementUnit,
      final List<List<Object>> parameterSets) throws SQLException {
    this.parameterSets = parameterSets;
    this.baseStatementUnit = baseStatementUnit;
   this.txcVisitor = TxcVisitorFactory.getSqlVisitor(baseStatementUnit.getStatement().getTxcConnection(),
        baseStatementUnit.getSqlExecutionUnit().getSql());
  }


  public TxcTable beforeExecute()

      throws SQLException {
    if (!DtsContext.inTxcTransaction()) {
      return null;
    }

    baseStatementUnit.getStatement().getTxcConnection().setAutoCommit(false);

    TxcTable nRet = null;
    switch (baseStatementUnit.getSqlExecutionUnit().getSqlType()) {
      case DELETE:
      case UPDATE:
      case INSERT:
        // 获取前置镜像
        txcVisitor.executeAndGetFrontImage(baseStatementUnit.getStatement().getRawStatement());
        break;
      default:
        break;
    }
    return nRet;
  }

  public TxcTable afterExecute()

      throws SQLException {
    if (!DtsContext.inTxcTransaction()) {
      return null;
    }

    TxcTable nRet = null;
    switch (baseStatementUnit.getSqlExecutionUnit().getSqlType()) {
      case DELETE:
      case UPDATE:
      case INSERT:
        // 获取前置镜像
        txcVisitor.executeAndGetRearImage(baseStatementUnit.getStatement().getRawStatement());
        insertUndoLog();
        break;
      default:
        break;
    }
    return nRet;
  }

  public void insertUndoLog() throws SQLException {
    // 对于空操作，直接返回成功，不写Log
    if (txcVisitor.getTableOriginalValue().getLinesNum() == 0 && txcVisitor.getTablePresentValue().getLinesNum() == 0 && txcVisitor.getRollbackRule() == null) {
      String errorInfo = "null result error:" + txcVisitor.getInputSql();
      logger.error("insertUndoLog", errorInfo);
      throw new DtsException(3333, errorInfo);
    }

    // 写入UndoLog
    RollbackInfor txcLog = new RollbackInfor();
    txcLog.setSql(txcVisitor.getInputSql());
    txcLog.setSqlType(txcVisitor.getSqlType());
    txcLog.setSelectSql(txcVisitor.getSelectSql());
    txcLog.setOriginalValue(txcVisitor.getTableOriginalValue());
    txcLog.setPresentValue(txcVisitor.getTablePresentValue());
    txcLog.setRollbackRule(txcVisitor.getRollbackRule());
    switch (txcVisitor.getSqlType()) {
      case DELETE:
        txcLog.setWhereCondition(txcVisitor.getWhereCondition(txcVisitor.getTableOriginalValue()));
        break;
      case UPDATE:
        txcLog.setWhereCondition(txcVisitor.getWhereCondition(txcVisitor.getTableOriginalValue()));
        break;
      case INSERT:
        txcLog.setWhereCondition(txcVisitor.getWhereCondition(txcVisitor.getTablePresentValue()));
        break;
      default:
        throw new DtsException("unknown error");
    }
    txcLog.txcLogChecker(); // json合法性检查

    baseStatementUnit.getStatement().getTxcConnection().getTxcContext().addInfor(txcLog);
  }




}
