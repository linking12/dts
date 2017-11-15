package io.dts.server.store.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.dts.server.store.DtsLogDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.GlobalLog;

@Repository
public class DtsLogDaoImpl implements DtsLogDao {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  public void insertGlobalLog(final GlobalLog globalLog) {
    PreparedStatementCreator psc = new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "insert into dts_global_log (state,gmt_created,gmt_modified) values (?,now(),now())",
            Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, globalLog.getState());
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
  public void updateGlobalLog(GlobalLog globalLog) {
    jdbcTemplate.update("update dts_global_log set state =?, gmt_modified= now() where tx_id = ?",
        new Object[] {globalLog.getState(), globalLog.getTransId()});
  }

  @Override
  public void deleteGlobalLog(long tx_id) {
    jdbcTemplate.update("delete from dts_global_log where tx_id = ?", new Object[] {tx_id});
  }

  @Override
  public GlobalLog getGlobalLog(long tx_id) {
    return jdbcTemplate.queryForObject("select * from dts_global_log where tx_id = ?",
        new Object[] {tx_id}, new RowMapper<GlobalLog>() {
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
  public void insertBranchLog(BranchLog branchLog) {
    PreparedStatementCreator psc = new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "insert into dts_branch_log (tx_id,state,client_ip,client_info,gmt_created,gmt_modified)"
                + " values (?,?,?,?,now(),now())",
            Statement.RETURN_GENERATED_KEYS);
        ps.setLong(1, branchLog.getTransId());
        ps.setInt(2, branchLog.getState());
        ps.setString(3, branchLog.getClientIp());
        ps.setString(4, branchLog.getClientInfo());
        return ps;
      }
    };
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(psc, keyHolder);
    long branchId = keyHolder.getKey().longValue();
    branchLog.setBranchId(branchId);
    branchLog.setGmtCreated(Calendar.getInstance().getTime());
    branchLog.setGmtModified(Calendar.getInstance().getTime());
  }



  @Override
  public void updateBranchLog(BranchLog branchLog) {
    jdbcTemplate.update("update dts_branch_log set state = ? where branch_id = ?",
        new Object[] {branchLog.getState(), branchLog.getBranchId()});
  }

  @Override
  public void updateBranchState(BranchLog branchLog) {
    jdbcTemplate.update("update dts_branch_log set state = ?" + " where branch_id = ?",
        new Object[] {branchLog.getState(), branchLog.getBranchId()});
  }

  @Override
  public List<BranchLog> getBranchLogs(long txId) {
    return jdbcTemplate.query("select * from dts_branch_log where tx_id = ?", new Object[] {txId},
        new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rowToObject(rs);
          }
        });
  }

  @Override
  public List<BranchLog> getBranchLogs() {
    return jdbcTemplate.query("select * from dts_branch_log", new Object[] {},
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
    log.setClientInfo(rs.getString("client_info"));
    log.setGmtCreated(rs.getTimestamp("gmt_created"));
    log.setGmtModified(rs.getTimestamp("gmt_modified"));
    return log;
  }

  @Override
  public BranchLog getBranchLog(long branchId) {
    return jdbcTemplate.queryForObject("select * from dts_branch_log where branch_id = ?",
        new Object[] {branchId}, new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rowToObject(rs);
          }
        });
  }

  @Override
  public List<GlobalLog> getGlobalLogs() {
    return jdbcTemplate.query("select * from dts_global_log", new Object[] {},
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
  public void insertBranchErrorLog(final BranchLog branchLog) {
    jdbcTemplate.update(
        "insert into dts_branch_error_log (branch_id,tx_id,state,client_ip,client_info,gmt_created,gmt_modified) values (?,?,?,?,?,now(),now())",
        new Object[] {branchLog.getBranchId(), branchLog.getTransId(), branchLog.getState(),
            branchLog.getClientIp(), branchLog.getClientInfo()});
  }

  @Override
  public void updateBranchErrorLog(BranchLog branchLog) {
    jdbcTemplate.update(
        "update dts_branch_error_log set tx_id=?,state=?,client_ip=?,client_info=?,gmt_modified=now(),is_notify= ? where branch_id=?",
        new Object[] {branchLog.getTransId(), branchLog.getState(), branchLog.getClientIp(),
            branchLog.getClientInfo(), branchLog.getIsNotify(), branchLog.getBranchId()});
  }

  @Override
  public List<BranchLog> findWaitNotifyErrorLog(int commit_type) {
    return jdbcTemplate.query(
        "select * from dts_branch_error_log where is_notify<>1 order by client_ip",
        new Object[] {commit_type}, new RowMapper<BranchLog>() {
          @Override
          public BranchLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            BranchLog log = new BranchLog();
            log.setTransId(rs.getLong("tx_id"));
            log.setState(rs.getInt("state"));
            log.setClientInfo(rs.getString("client_info"));
            log.setClientIp(rs.getString("client_ip"));
            log.setBranchId(rs.getLong("branch_id"));
            log.setGmtCreated(rs.getTimestamp("gmt_created"));
            log.setGmtModified(rs.getTimestamp("gmt_modified"));
            return log;
          }
        });
  }

  @Override
  public void deleteBranchLog(long branchId) {
    jdbcTemplate.update("delete from dts_branch_log where branch_id = ?", new Object[] {branchId});
  }

}
