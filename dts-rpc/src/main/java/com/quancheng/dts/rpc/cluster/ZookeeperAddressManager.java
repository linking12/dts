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

import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.quancheng.dts.exception.DtsException;

import java.util.List;

/**
 * @author liushiming
 * @version AddressManagerZkmpl.java, v 0.0.1 2017年7月21日 下午4:41:04 liushiming
 * @since JDK 1.8
 */
public class ZookeeperAddressManager extends AbstractVirtualGroupAddressManager {

  private static Logger logger = LoggerFactory.getLogger(ZookeeperAddressManager.class);

  private static final int DEFAULT_SESSION_TIMEOUT = 15000;

  private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

  private static final String SEPARATOR = "/";

  private CuratorFramework client;

  private String registerRootPath;

  private String clusterAddress;

  public ZookeeperAddressManager() {

  }

  public ZookeeperAddressManager(final String clusterAddress, final String registerRootPath) {
    this.registerRootPath = registerRootPath;
    this.clusterAddress = clusterAddress;
    init();
  }

  public void init() {
    synchronized (ZookeeperAddressManager.class) {
      initConnection(clusterAddress);
    }
  }

  public void destroy() {
    logger.debug("Destroy CuratorRegisterClient ...");
    if (client != null && client.getState().equals(CuratorFrameworkState.STARTED)) {
      client.close();
      client = null;
    }
    logger.info("Destroy CuratorRegisterClient OK!");
  }

  private void initConnection(final String connectString) {
    if (StringUtils.isEmpty(connectString)) {
      throw new DtsException("Missing Connection String Or RegisterRootPath.");
    }
    try {
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(DEFAULT_SESSION_TIMEOUT, 3);
      client = CuratorFrameworkFactory.builder()
                                      .connectString(connectString)
                                      .retryPolicy(retryPolicy)
                                      .connectionTimeoutMs(DEFAULT_CONNECTION_TIMEOUT)
                                      .build();
      client.start();

      client.getConnectionStateListenable().addListener((client, newState) -> {
        switch (newState) {
          case CONNECTED:
            new Thread(() -> createRootNode(registerRootPath)).start();
            logger.debug("CuratorFramework Connection CONNECTED.");
            break;
          case LOST:
            logger.error("CuratorFramework Connection LOST.");
            break;
          case SUSPENDED:
            logger.error("CuratorFramework Connection SUSPENDED.");
            break;
          case RECONNECTED:
            logger.debug("CuratorFramework Connection RECONNECTED");
            new Thread(() -> createRootNode(registerRootPath)).start();
            break;
          default:
            break;
        }
      });
    } catch (Exception e) {
      logger.error("CuratorRegisterClient Initial Connection Exception, exception = {}", e);
    }
    logger.debug("CuratorRegisterClient Init Connection OK.");
  }


  private void createRootNode(String registerRootPath) {
    String path = registerRootPath;
    try {
      if (null == client.checkExists().forPath(path)) {
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
      }
    } catch (Exception e) {
      //should never happen.

      logger.error("Create root path Error, error:{}", e);
    }
    logger.info("Create RootNode OK.");
  }

  private void createTempNode(String group, String registerInfo) {
      String path = registerRootPath + SEPARATOR + group + SEPARATOR + registerInfo;
      try {
        if (null == client.checkExists().forPath(path)) {
          client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,
              registerInfo.getBytes("UTF-8"));
        }
      } catch (Exception e) {
        //should never happen.

        logger.error("Create TempNode Error, error:{}", e);
      }
    logger.info("Create TempNode OK.");
  }

  private void removeTempNode(String group, String registerInfo) {
      String path = registerRootPath + SEPARATOR + group + SEPARATOR + registerInfo;
      try {
        if (client.checkExists().forPath(path) != null) {
          client.delete().inBackground().forPath(path);
        }
      } catch (Exception e) {
        //should never happen.

        logger.error("Create TempNode Error, error:{}", e);
      }
    logger.info("Create TempNode OK.");
  }

  /**
   * @see com.quancheng.dts.rpc.cluster.AddressManager#publish(java.lang.String, java.lang.String) 
   */  
  @Override
  public void publish(String group, String address) {
    // TODO Auto-generated method stub
    this.createTempNode(group, address);
  }

  /**  
   * @see com.quancheng.dts.rpc.cluster.AddressManager#unpublish(java.lang.String, java.lang.String) 
   */  
  @Override
  public void unpublish(String group, String address) {
    // TODO Auto-generated method stub
    this.removeTempNode(group, address);
  }

  /**  
   * @see com.quancheng.dts.rpc.cluster.AbstractVirtualGroupAddressManager#getAddressListFromStorage(java.lang.String) 
   */  
  @Override
  public List<String> getAddressListFromStorage(String rGroup) throws InterruptedException {
    // TODO Auto-generated method stub
    String path = registerRootPath + SEPARATOR + rGroup;
    try {
      return client.getChildren().forPath(path);
    } catch (KeeperException.NoNodeException e) {
      logger.warn("Node is not found when get children for path = {}, msg = {}", path, e.getMessage());
    } catch (Exception e) {
      logger.error("Exception caught when get children for path = {}", path, e);
    }
    return Lists.newArrayList();
  }

  /**  
   * @see com.quancheng.dts.rpc.cluster.AbstractVirtualGroupAddressManager#getAddressListFromStorage(java.lang.String, com.quancheng.dts.rpc.cluster.AddressWatcher) 
   */  
  @Override
  public void getAddressListFromStorage(String rGroup, AddressWatcher watcher)
      throws InterruptedException {
    // TODO Auto-generated method stub
    watcher.onAddressListChanged(getAddressListFromStorage(rGroup));
  }

}
