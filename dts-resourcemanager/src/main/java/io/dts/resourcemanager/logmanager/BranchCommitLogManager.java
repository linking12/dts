/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.resourcemanager.logmanager;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import io.dts.resourcemanager.DataSourceHolder;
import io.dts.resourcemanager.struct.ContextStep2;
import io.dts.resourcemanager.struct.UndoLogMode;

/**
 * @author liushiming
 * @version BranchCommitLogManager.java, v 0.0.1 2017年10月24日 下午3:52:14 liushiming
 */
public class BranchCommitLogManager extends DtsLogManagerImpl {

  private static final Logger logger = LoggerFactory.getLogger(BranchCommitLogManager.class);

  @Override
  public void branchCommit(ContextStep2 context) throws SQLException {
    DataSource datasource = DataSourceHolder.getDataSource(context.getDbname());
    DataSourceTransactionManager tm = new DataSourceTransactionManager(datasource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
    final JdbcTemplate template = new JdbcTemplate(datasource);
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        String deleteSql = String.format("delete from %s where id in (%s) and status = %d",
            txcLogTableName, context.getGlobalXid(), UndoLogMode.COMMON_LOG.getValue());
        logger.info("delete undo log sql:" + deleteSql);
        template.execute(deleteSql);
      }
    });
  }



}
