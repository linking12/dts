package io.dts.server.store.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import io.dts.common.common.CommitMode;
import io.dts.common.util.BlobUtil;
import io.dts.server.config.AppConfig;
import io.dts.server.store.DtsLogDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.GlobalLog;

@Repository
public class DtsLogDaoImpl implements DtsLogDao {

  private static final Logger logger = LoggerFactory.getLogger(DtsLogDaoImpl.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private TransactionTemplate transactionTemplate;


  @Override
  public void insertGlobalLog(final GlobalLog globalLog, final int mid) {
    PreparedStatementCreator psc = new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "insert into dts_global_log (state,mid,gmt_created,gmt_modified) values (?,?,now(),now())",
            Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, globalLog.getState());
        ps.setInt(2, mid);

        return ps;
      }
    };

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(psc, keyHolder);

    long txId = keyHolder.getKey().longValue();

    globalLog.setTransId(txId);
    globalLog.setGmtCreated(Calendar.getInstance().getTime());
    globalLog.setGmtModified(Calendar.getInstance().getTime());
  }

  @Override
  public void updateGlobalLog(GlobalLog globalLog, int mid) {
    jdbcTemplate.update(
        "update dts_global_log set state = ? , gmt_modified= now() where tx_id = ? and mid = ?",
        new Object[] {globalLog.getState(), globalLog.getTransId(), mid});
  }

  @Override
  public void deleteGlobalLog(long tx_id, int mid) {
    jdbcTemplate.update("delete from dts_global_log where tx_id = ? and mid = ?",
        new Object[] {tx_id, mid});
  }

  @Override
  public GlobalLog getGlobalLog(long tx_id, int mid) {
    return jdbcTemplate.queryForObject("select * from dts_global_log where tx_id = ? and mid = ?",
        new Object[] {tx_id, mid}, new RowMapper<GlobalLog>() {
          @Override
          public GlobalLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            GlobalLog log = new GlobalLog();

            log.setTransId(rs.getLong("tx_id"));
            log.setState(rs.getInt("state"));
            log.setGmtCreated(rs.getTimestamp("gmt_created"));
            log.setGmtModified(rs.getTimestamp("gmt_modified"));
            return log;
          }
        });
  }

  @Override
  public void insertBranchLog(BranchLog branchLog, int mid) {
    if (branchLog.getCommitMode() == CommitMode.COMMIT_RETRY_MODE.getValue()) {
      insertRtBranchLog(branchLog, mid);
    } else {
      insertBranchLog0(branchLog, mid);
    }
  }

  private void insertBranchLog0(final BranchLog branchLog, final int mid) {
    PreparedStatementCreator psc = new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "insert into dts_branch_log (tx_id,state,client_ip,client_app_name,client_info,gmt_created,gmt_modified,commit_mode,udata,mid)"
                + " values (?,?,?,?,?, now(),now(),?,?,?)",
            Statement.RETURN_GENERATED_KEYS);

        ps.setLong(1, branchLog.getTransId());
        ps.setInt(2, branchLog.getState());
        ps.setString(3, branchLog.getClientIp());
        ps.setString(4, branchLog.getClientAppName());
        ps.setString(5, branchLog.getClientInfo());
        ps.setInt(6, branchLog.getCommitMode());
        ps.setString(7, branchLog.getUdata());
        ps.setInt(8, mid);
        return ps;
      }
    };

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(psc, keyHolder);
    long branchId = keyHolder.getKey().longValue();
    branchLog.setBranchId(branchId);
    if (branchLog.getCommitMode() == CommitMode.COMMIT_RETRY_MODE.getValue()) {
      branchLog.setGmtCreated(Calendar.getInstance().getTime());
      branchLog.setGmtModified(Calendar.getInstance().getTime());
    }
  }

  private void insertRtBranchLog(final BranchLog rtBranchLog, final int mid) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          logger.info(
              "insertRtBranchLog:" + rtBranchLog.getBranchId() + ":" + rtBranchLog.getRetrySql());

          insertBranchLog0(rtBranchLog, mid);
          jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
              PreparedStatement ps = con.prepareStatement(
                  "insert into dts_rt_sql (tx_id,branch_id,rt_sql,mid,gmt_created,gmt_modified)"
                      + " values (?,?,?,?,now(),now())");

              ps.setLong(1, rtBranchLog.getTransId());
              ps.setLong(2, rtBranchLog.getBranchId());
              ps.setBlob(3, BlobUtil.string2blob(rtBranchLog.getRetrySql()));
              ps.setInt(4, mid);
              return ps;
            }
          });
        } catch (DataAccessException ex) {
          status.setRollbackOnly();
          throw ex;
        }
      }
    });
  }

  @Override
  public void updateBranchLog(BranchLog branchLog, int mid) {
    jdbcTemplate.update(
        "update dts_branch_log set state = ?, udata = ?" + " where branch_id = ? and mid = ?",
        new Object[] {branchLog.getState(), branchLog.getUdata(), branchLog.getBranchId(), mid});
  }

  @Override
  public void updateBranchState(BranchLog branchLog, int mid) {
    jdbcTemplate.update("update dts_branch_log set state = ?" + " where branch_id = ? and mid = ?",
        new Object[] {branchLog.getState(), branchLog.getBranchId(), mid});
  }

  @Override
  public List<BranchLog> getBranchLogs(long txId, int mid) {
    return jdbcTemplate.query("select * from dts_branch_log where tx_id = ? and mid = ?",
        new Object[] {txId, mid}, new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rowToObject(rs);
          }
        });
  }

  @Override
  public List<BranchLog> getBranchLogs(int mid) {
    return jdbcTemplate.query("select * from dts_branch_log where mid = ?", new Object[] {mid},
        new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rowToObject(rs);
          }
        });
  }

  private BranchLog rowToObject(ResultSet rs) throws SQLException {
    BranchLog log = new BranchLog();
    log.setBranchId(rs.getLong("branch_id"));
    log.setTransId(rs.getLong("tx_id"));
    log.setState(rs.getInt("state"));
    log.setClientIp(rs.getString("client_ip"));
    log.setClientAppName(rs.getString("client_app_name"));
    log.setClientInfo(rs.getString("client_info"));
    log.setCommitMode(rs.getInt("commit_mode"));
    log.setUdata(rs.getString("udata"));
    log.setGmtCreated(rs.getTimestamp("gmt_created"));
    log.setGmtModified(rs.getTimestamp("gmt_modified"));

    if (log.getCommitMode() == CommitMode.COMMIT_RETRY_MODE.getValue()) {
      List<String> rtSqls =
          jdbcTemplate.query("select rt_sql from dts_rt_sql where branch_id = " + log.getBranchId(),
              new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return BlobUtil.blob2string(rs.getBlob("rt_sql"));
                }
              });
      log.setRetrySql(rtSqls.size() > 0 ? (String) rtSqls.get(0) : null);
    }

    return log;
  }

  @Override
  public BranchLog getBranchLog(long branchId, int mid) {
    return jdbcTemplate.queryForObject(
        "select * from dts_branch_log where branch_id = ? and mid = ?",
        new Object[] {branchId, mid}, new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rowToObject(rs);
          }
        });
  }

  @Override
  public List<GlobalLog> getGlobalLogs(int mid) {
    return jdbcTemplate.query("select * from dts_global_log where mid=?", new Object[] {mid},
        new RowMapper<GlobalLog>() {
          @Override
          public GlobalLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            GlobalLog log = new GlobalLog();
            log.setTransId(rs.getLong("tx_id"));
            log.setState(rs.getInt("state"));
            log.setGmtCreated(rs.getTimestamp("gmt_created"));
            log.setGmtModified(rs.getTimestamp("gmt_modified"));
            return log;
          }
        });
  }

  @Override
  public void insertBranchErrorLog(final BranchLog branchLog, final int mid) {
    String appName = branchLog.getClientAppName();

    jdbcTemplate.update(
        "insert into dts_branch_error_log ("
            + "branch_id,tx_id,state,client_ip,client_app_name,client_info,"
            + "gmt_created,gmt_modified,commit_mode,rt_sql,mid)"
            + " values (?,?,?,?,?,?, now(),now(),?,?,?)",
        new Object[] {branchLog.getBranchId(), branchLog.getTransId(), branchLog.getState(),
            branchLog.getClientIp(), appName, branchLog.getClientInfo(), branchLog.getCommitMode(),
            BlobUtil.string2blob(branchLog.getRetrySql()), mid});
  }

  @Override
  public void updateBranchErrorLog(BranchLog branchLog, int mid) {
    jdbcTemplate.update(
        "update dts_branch_error_log  set "
            + "tx_id=?,state=?,client_ip=?,client_app_name=?,client_info=?,gmt_modified=now(),"
            + "commit_mode= ?,is_notify= ?,rt_sql=? where branch_id=? and mid=?",
        new Object[] {branchLog.getTransId(), branchLog.getState(), branchLog.getClientIp(),
            branchLog.getClientAppName(), branchLog.getClientInfo(), branchLog.getCommitMode(),
            branchLog.getIsNotify(), BlobUtil.string2blob(branchLog.getRetrySql()),
            branchLog.getBranchId(), mid});
  }

  @Override
  public List<BranchLog> findWaitNotifyErrorLog(int commit_type) {
    return jdbcTemplate.query(
        "select * from dts_branch_error_log where is_notify<>1 and commit_mode=? and mid=? "
            + "order by client_app_name",
        new Object[] {commit_type, AppConfig.mId}, new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            BranchLog log = new BranchLog();
            log.setTransId(rs.getLong("tx_id"));
            log.setState(rs.getInt("state"));
            log.setClientAppName(rs.getString("client_app_name"));
            log.setClientInfo(rs.getString("client_info"));
            log.setClientIp(rs.getString("client_ip"));
            log.setBranchId(rs.getLong("branch_id"));
            log.setCommitMode(rs.getInt("commit_mode"));
            log.setGmtCreated(rs.getTimestamp("gmt_created"));
            log.setGmtModified(rs.getTimestamp("gmt_modified"));
            log.setRetrySql(BlobUtil.blob2string(rs.getBlob("rt_sql")));
            return log;
          }
        });
  }

  @Override
  public void deleteBranchLog(BranchLog branchLog, int mid) {
    if (branchLog.getCommitMode() == CommitMode.COMMIT_RETRY_MODE.getValue()) {
      deleteRtBranchLog(branchLog.getBranchId(), mid);
    } else {
      deleteBranchLog(branchLog.getBranchId(), mid);
    }
  }

  private void deleteBranchLog(long branchId, int mid) {
    jdbcTemplate.update("delete from dts_branch_log where branch_id = ? and mid = ?",
        new Object[] {branchId, mid});
  }

  private void deleteRtBranchLog(final long branchId, final int mid) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          deleteBranchLog(branchId, mid);
          jdbcTemplate.update("delete from dts_rt_sql where branch_id = ?",
              new Object[] {branchId});
        } catch (DataAccessException ex) {
          status.setRollbackOnly();
          throw ex;
        }
      }
    });
  }
}
