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
package io.dts.server.store;

import java.util.List;

import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;

/**
 * @author liushiming
 * @version DtsTranStateDao.java, v 0.0.1 2017年9月18日 下午4:01:20 liushiming
 */
public interface DtsTransStatusDao {

  public void insertGlobalLog(Long transId, GlobalLog globalLog);

  public void clearGlobalLog(Long transId);

  public void insertBranchLog(Long branchId, BranchLog branchLog);

  public void clearBranchLog(Long branchId);

  public void insertCommitedBranchLog(Long branchId, Integer commitResultCode);

  public void insertRollbackBranchLog(Long branchId, Integer rollbackingResultCode);

  public GlobalLog queryGlobalLog(Long transId);

  public BranchLog queryBranchLog(Long branchId);

  public List<BranchLog> queryBranchLogByTransId(Long transId);

  public boolean queryTimeOut(Long transId);

  public boolean removeTimeOut(Long transId);

}
