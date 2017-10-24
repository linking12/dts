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
package io.dts.resourcemanager.logmanager;

import java.sql.SQLException;
import java.util.List;

import io.dts.common.common.context.ContextStep2;

/**
 * @author liushiming
 * @version DtsLogManager.java, v 0.0.1 2017年10月24日 下午3:22:15 liushiming
 */
public interface DtsLogManager {

  /**
   * 分支事务提交，仅删除UndoLog
   */
  void branchCommit(List<ContextStep2> contexts) throws SQLException;

  /**
   * 分支事务回滚，回滚阶段的数据库操作在一个本地事务中执行
   */
  void branchRollback(ContextStep2 context) throws SQLException;


  public static DtsLogManager getInstance() {
    return DtsLogManagerImpl.logManager;
  }
}
