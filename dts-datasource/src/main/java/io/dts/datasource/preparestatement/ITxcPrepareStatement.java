package io.dts.datasource.preparestatement;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import io.dts.datasource.connection.ITxcConnection;
import io.dts.datasource.statement.ITxcStatement;

public interface ITxcPrepareStatement extends ITxcStatement, PreparedStatement {


  PreparedStatement getRawStatement() throws SQLException;

  String getTargetSql();

}
