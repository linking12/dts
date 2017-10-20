package io.dts.resourcemanager.log;

import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import io.dts.common.common.context.ContextStep2;

public interface IDtsLogManager {
  /**
   * 分支事务提交，仅删除UndoLog
   */
  void branchCommit(List<ContextStep2> contexts) throws SQLException;

  /**
   * 分支事务回滚，回滚阶段的数据库操作在一个本地事务中执行
   */
  void branchRollback(ContextStep2 context) throws SQLException;

  /**
   * 清理事务日志
   */
  void deleteUndoLog(ContextStep2 context, JdbcTemplate template) throws SQLException;

  void deleteUndoLog(List<ContextStep2> contexts, JdbcTemplate template) throws SQLException;


  public static IDtsLogManager getInstance() {
    return DtsLogManager.getInstance();
  }
}
