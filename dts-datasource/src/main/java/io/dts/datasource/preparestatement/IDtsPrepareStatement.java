package io.dts.datasource.preparestatement;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.dts.datasource.statement.IDtsStatement;

public interface IDtsPrepareStatement extends IDtsStatement, PreparedStatement {


  PreparedStatement getRawStatement() throws SQLException;

  String getTargetSql();

}
