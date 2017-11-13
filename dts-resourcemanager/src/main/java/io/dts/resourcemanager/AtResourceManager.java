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
package io.dts.resourcemanager;

import java.sql.SQLException;

import io.dts.common.common.DtsXID;
import io.dts.common.exception.DtsException;
import io.dts.resourcemanager.logmanager.DtsLogManager;
import io.dts.resourcemanager.struct.ContextStep2;

/**
 * @author liushiming
 * @version AtResourceManager.java, v 0.0.1 2017年10月16日 下午3:18:14 liushiming
 */
public class AtResourceManager extends BaseResourceManager {

  @Override
  public String branchCommit(String xid, long branchId, String key, String udata, int commitMode,
      String retrySql) throws DtsException {
    try {
      ContextStep2 context = new ContextStep2();
      context.setXid(xid);
      context.setBranchId(branchId);
      context.setDbname(key);
      context.setUdata(udata);
      context.setRetrySql(retrySql);
      context.setGlobalXid(DtsXID.getGlobalXID(xid, branchId));
      DtsLogManager.getInstance().branchCommit(context);
      return context.getReportSql();
    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    }
  }

  @Override
  public String branchRollback(String xid, long branchId, String key, String udata, int commitMode)
      throws DtsException {
    ContextStep2 context = new ContextStep2();
    context.setXid(xid);
    context.setBranchId(branchId);
    context.setDbname(key);
    context.setUdata(udata);
    context.setGlobalXid(DtsXID.getGlobalXID(xid, branchId));
    try {
      DtsLogManager.getInstance().branchRollback(context);
      return context.getReportSql();
    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    }
  }


}
