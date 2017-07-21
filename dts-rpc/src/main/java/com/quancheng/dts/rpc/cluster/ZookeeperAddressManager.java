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
package com.quancheng.dts.rpc.cluster;

/**
 * @author liushiming
 * @version AddressManagerZkmpl.java, v 0.0.1 2017年7月21日 下午4:41:04 liushiming
 * @since JDK 1.8
 */
public class ZookeeperAddressManager extends AbstractVirtualGroupAddressManager {

  /**  
   * @see com.quancheng.dts.rpc.cluster.AddressManager#publish(java.lang.String, java.lang.String) 
   */  
  @Override
  public void publish(String group, String address) {
    // TODO Auto-generated method stub
    
  }

  /**  
   * @see com.quancheng.dts.rpc.cluster.AddressManager#unpublish(java.lang.String, java.lang.String) 
   */  
  @Override
  public void unpublish(String group, String address) {
    // TODO Auto-generated method stub
    
  }

  /**  
   * @see com.quancheng.dts.rpc.cluster.AbstractVirtualGroupAddressManager#getAddressListFromStorage(java.lang.String) 
   */  
  @Override
  public void getAddressListFromStorage(String rGroup) throws InterruptedException {
    // TODO Auto-generated method stub
    
  }

  /**  
   * @see com.quancheng.dts.rpc.cluster.AbstractVirtualGroupAddressManager#getAddressListFromStorage(java.lang.String, com.quancheng.dts.rpc.cluster.AddressWatcher) 
   */  
  @Override
  public void getAddressListFromStorage(String rGroup, AddressWatcher watcher)
      throws InterruptedException {
    // TODO Auto-generated method stub
    
  }

}
