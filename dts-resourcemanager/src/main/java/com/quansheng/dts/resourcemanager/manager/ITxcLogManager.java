package com.quansheng.dts.resourcemanager.manager;

import org.springframework.jdbc.core.JdbcTemplate;

import com.quancheng.dts.context.ContextStep2;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by guoyubo on 2017/9/12.
 */
public interface ITxcLogManager {
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

}
