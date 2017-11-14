package io.dts.server.store;


import java.util.List;

import io.dts.server.struct.BranchLog;
import io.dts.server.struct.GlobalLog;

public interface DtsLogDao {

  public void insertGlobalLog(GlobalLog globalLog);

  public void updateGlobalLog(GlobalLog globalLog);

  public void deleteGlobalLog(long tx_id);

  public GlobalLog getGlobalLog(long tx_id);

  public List<GlobalLog> getGlobalLogs();

  public void insertBranchLog(BranchLog branchLog);

  public void updateBranchLog(BranchLog branchLog);

  public void updateBranchState(BranchLog branchLog);

  public void deleteBranchLog(long branchId);

  public List<BranchLog> getBranchLogs(long txId);

  public List<BranchLog> getBranchLogs();

  public BranchLog getBranchLog(long branchId);

  public void insertBranchErrorLog(BranchLog branchLog);

  public void updateBranchErrorLog(BranchLog branchLog);

  public List<BranchLog> findWaitNotifyErrorLog(int commit_type);
}
