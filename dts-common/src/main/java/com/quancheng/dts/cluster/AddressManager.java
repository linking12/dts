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
package com.quancheng.dts.cluster;

/**
 * @author liushiming
 * @version AddressManager.java, v 0.0.1 2017年7月21日 下午3:48:55 liushiming
 * @since JDK 1.8
 */
public interface AddressManager {

  public void publish(String group, String address);

  public void unpublish(String group, String address);

  public void getAddressList(String group, AddressWatcher watcher) throws InterruptedException;
}
