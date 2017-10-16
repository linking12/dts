package io.dts.resourcemanager.api;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import io.dts.common.common.RedoContext.BuildBranchId;
import io.dts.common.common.RedoContext.RedoBranch;
import io.dts.resourcemanager.redo.CpToMirroSql;


public interface SqlExecutor<T> {
  public T execute(Statement tddlSt, Object... args) throws SQLException;

  public T executeNewRedo(BuildBranchId builder, RedoBranch branch, Statement tddlSt,
      List<CpToMirroSql> cpSqls, List<String> sql) throws SQLException;

  public T mockExecute(int num) throws SQLException;
}
