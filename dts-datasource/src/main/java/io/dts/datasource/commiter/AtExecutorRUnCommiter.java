package io.dts.datasource.commiter;


import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.common.context.DtsContext;
import io.dts.common.common.exception.DtsException;
import io.dts.datasource.wrapper.executor.StatementModel;
import io.dts.parser.TxcVisitorFactory;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcTable;
import io.dts.parser.vistor.ITxcVisitor;
import io.dts.resourcemanager.api.IDtsConnection;

public class AtExecutorRUnCommiter {

  private static final Logger logger = LoggerFactory.getLogger(AtExecutorRUnCommiter.class);

  private ITxcVisitor txcVisitor;

  private StatementModel stateModel;


  public AtExecutorRUnCommiter(StatementModel stateModel, final List<Object> parameterSet)
      throws SQLException {
    this.stateModel = stateModel;
    IDtsConnection txcConnection = stateModel.getStatement().getDtsConnection();
    this.txcVisitor =
        TxcVisitorFactory.createSqlVisitor(txcConnection.getDataSource().getDatabaseType(),
            txcConnection.getRawConnection(), stateModel.getSql(), parameterSet);

  }


  public TxcTable beforeExecute() throws SQLException {
    if (!DtsContext.inTxcTransaction()) {
      return null;
    }
    TxcTable nRet = null;
    switch (stateModel.getSqlType()) {
      case DELETE:
      case UPDATE:
      case INSERT:
        this.txcVisitor.buildTableMeta();
        // 获取前置镜像
        txcVisitor.executeAndGetFrontImage(stateModel.getStatement().getRawStatement());
        break;
      default:
        break;
    }
    return nRet;
  }

  public TxcTable afterExecute() throws SQLException {
    if (!DtsContext.inTxcTransaction()) {
      return null;
    }

    TxcTable nRet = null;
    switch (stateModel.getSqlType()) {
      case DELETE:
      case UPDATE:
      case INSERT:
        // 获取前置镜像
        txcVisitor.executeAndGetRearImage(stateModel.getStatement().getRawStatement());
        insertUndoLog();
        break;
      default:
        break;
    }
    return nRet;
  }

  private void insertUndoLog() throws SQLException {
    // 对于空操作，直接返回成功，不写Log
    if (txcVisitor.getTableOriginalValue().getLinesNum() == 0
        && txcVisitor.getTablePresentValue().getLinesNum() == 0) {
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
    stateModel.getStatement().getDtsConnection().getTxcContext().addInfor(txcLog);
  }



}
