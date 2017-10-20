package io.dts.resourcemanager.api;


import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IDtsPrepareStatement extends IDtsStatement, PreparedStatement {


  PreparedStatement getRawStatement() throws SQLException;

  String getTargetSql();

}
