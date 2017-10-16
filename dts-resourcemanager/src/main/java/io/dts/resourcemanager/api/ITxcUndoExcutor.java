package io.dts.resourcemanager.api;

import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;


public interface ITxcUndoExcutor {
  /**
   * 对用户输入SQL产生的作用进行补偿\回滚
   * 
   * @param template
   * @throws TxcException
   */
  public void rollback(JdbcTemplate template) throws SQLException;
}
