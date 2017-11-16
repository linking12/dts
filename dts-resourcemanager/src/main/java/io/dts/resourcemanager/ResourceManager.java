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

import io.dts.common.exception.DtsException;

/**
 * @author liushiming
 * @version ResourceManager.java, v 0.0.1 2017年10月13日 下午2:23:40 liushiming
 */
public interface ResourceManager {

  public long register(String key) throws DtsException;

  public String getRegisterKey();

  public void branchCommit(String xid, long branchId, String resourceInfo) throws DtsException;

  public void branchRollback(String xid, long branchId, String resourceInfo) throws DtsException;

}
