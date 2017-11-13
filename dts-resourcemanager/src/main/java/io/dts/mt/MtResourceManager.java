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
package io.dts.mt;

import io.dts.common.exception.DtsException;
import io.dts.resourcemanager.BaseResourceManager;

/**
 * @author liushiming
 * @version MtResourceManager.java, v 0.0.1 2017年10月16日 下午3:16:01 liushiming
 */
public class MtResourceManager extends BaseResourceManager {

  @Override
  public void branchCommit(String xid, long branchId, String key, String udata, int commitMode,
      String retrySql) throws DtsException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode)
      throws DtsException {
    // TODO Auto-generated method stub
    
  }



}
