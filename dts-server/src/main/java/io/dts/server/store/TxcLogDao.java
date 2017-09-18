package io.dts.server.store;


import java.util.List;

import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;

public interface TxcLogDao {

	public void insertGlobalLog(GlobalLog globalLog, int mid);

	public void updateGlobalLog(GlobalLog globalLog, int mid);

	public void deleteGlobalLog(long tx_id, int mid);

	public GlobalLog getGlobalLog(long tx_id, int mid);

	public List<GlobalLog> getGlobalLogs(int mid);

	public void insertBranchLog(BranchLog branchLog, int mid);

	public void updateBranchLog(BranchLog branchLog, int mid);

	public void updateBranchState(BranchLog branchLog, int mid);

	public void deleteBranchLog(BranchLog branchLog, int mid);

	public List<BranchLog> getBranchLogs(long txId, int mid);
	
	public List<BranchLog> getBranchLogs(int mid);

	public BranchLog getBranchLog(long branchId, int mid);

	public void insertBranchErrorLog(BranchLog branchLog, int mid);

	public void updateBranchErrorLog(BranchLog branchLog, int mid);

	public List<BranchLog> findWaitNotifyErrorLog(int commit_type);
}
