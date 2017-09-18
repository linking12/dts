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
package io.dts.server.store.impl;

import java.util.List;

import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version DtsTransStatusDaoImpl.java, v 0.0.1 2017年9月18日 下午4:14:49 liushiming
 */
public class DtsTransStatusDaoImpl implements DtsTransStatusDao {

  @Override
  public void insertGlobalLog(Long tranId, GlobalLog globalLog) {
    // TODO Auto-generated method stub

  }

  @Override
  public void insertBranchLog(Long branchId, BranchLog branchLog) {
    // TODO Auto-generated method stub

  }

  @Override
  public void insertCommitedBranchLog(Long branchId, Integer commitResultCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void insertRollbackBranchLog(Long branchId, Integer rollbackingResultCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<BranchLog> queryBranchLogByTransId(Long transId) {
    // TODO Auto-generated method stub
    return null;
  }

}
