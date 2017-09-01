package org.dts.datasource;

import org.dts.datasource.filter.RegisterBranchFilter;
import org.dts.datasource.filter.StatementExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.google.common.collect.Lists;
import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.RequestCode;
import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.request.RegisterMessage;
import com.quancheng.dts.message.response.RegisterResultMessage;
import com.quancheng.dts.rpc.remoting.DtsClient;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyubo on 2017/8/22.
 */
public class DtsDataSource extends DruidDataSource {

  private static final Logger log = LoggerFactory.getLogger(DtsDataSource.class);

  private DtsClient dtsClient;

  public DtsDataSource() {
    super();
  }

  public DtsDataSource(final boolean fairLock) {
    super(fairLock);
  }

  @Override
  public void init() throws SQLException {
    final List<StatementExecuteListener> statementExecuteListeners = new ArrayList<>();
    statementExecuteListeners.add(new StatementExecuteListener() {
      @Override
      public void beforeExecute(final StatementProxy statement, final String sql) {
        if (sql.startsWith("select") || sql.startsWith("SELECT")) {
          return;
        } else {
          final RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.BRANCH_REGISTER, null);
          RegisterMessage registerMessage = new RegisterMessage();
          registerMessage.setKey(DtsDataSource.this.getName());
          registerMessage.setTranId(DtsXID.getTransactionId(DtsContext.getCurrentXid()));
          request.setBody(RemotingSerializable.encode(registerMessage));
          try {
            RegisterResultMessage registerResultMessage = dtsClient.invokeSync(request, DtsDataSource.this.getMaxWait(), RegisterResultMessage.class);
            System.out.println(registerResultMessage);
            DtsContext.bindBranch(DtsDataSource.this.getName(), registerResultMessage.getBranchId());
          } catch (DtsException e) {
            log.error("register branch error", e);
          }
        }
      }

      @Override
      public void afterExecute(final StatementProxy statement, String sql, final Throwable error) {

      }


    });
    super.setProxyFilters(Lists.newArrayList(new RegisterBranchFilter(statementExecuteListeners)));
    super.init();
  }

  public void setDtsClient(final DtsClient dtsClient) {
    this.dtsClient = dtsClient;
  }
}
