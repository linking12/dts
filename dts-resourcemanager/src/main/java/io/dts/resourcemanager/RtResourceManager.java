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

import io.dts.common.common.exception.DtsException;

/**
 * @author liushiming
 * @version RtResourceManager.java, v 0.0.1 2017年10月16日 下午3:18:54 liushiming
 */
public class RtResourceManager extends BaseResourceManager {

  @Override
  public void reportUdata(String xid, long branchId, String key, String udata, boolean delay)
      throws DtsException {
    // TODO Auto-generated method stub

  }

  @Override
  public void branchCommit(String xid, long branchId, String key, String udata, byte commitMode,
      String retrySql) throws DtsException {
    // TODO Auto-generated method stub

  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, byte commitMode)
      throws DtsException {
    // TODO Auto-generated method stub

  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, byte commitMode,
      int isDelKey) throws DtsException {
    // TODO Auto-generated method stub

  }

}
