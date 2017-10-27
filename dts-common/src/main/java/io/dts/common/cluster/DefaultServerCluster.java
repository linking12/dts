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
package io.dts.common.cluster;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.dts.common.exception.DtsException;
import io.dts.common.util.NetUtil;

/**
 * @author liushiming
 * @version DefaultServerCluster.java, v 0.0.1 2017年10月17日 下午5:10:08 liushiming
 */
public class DefaultServerCluster implements ServerCluster {

  private static final Logger logger = LoggerFactory.getLogger(DefaultServerCluster.class);

  private static final String SERVERADDRESS_NODEPATH = "/dts/servers";

  private static final Set<String> SERVER_NODES = Sets.newConcurrentHashSet();

  private Random random = new Random(System.currentTimeMillis());

  private static ServerCluster serverCluster = new DefaultServerCluster();


  public DefaultServerCluster() {
    CuratorFramework curatorFramework = ZkClientFacotry.getZkClient();
    try {
      @SuppressWarnings("resource")
      final NodeCache cache = new NodeCache(curatorFramework, SERVERADDRESS_NODEPATH);
      cache.getListenable().addListener(new NodeCacheListener() {
        @Override
        public void nodeChanged() throws Exception {
          String path = cache.getCurrentData().getPath();
          SERVER_NODES.add(path);
        }
      }, Executors.newFixedThreadPool(1));
      cache.start(true);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static ServerCluster getInstance() {
    return serverCluster;
  }


  @Override
  public String select() {
    List<String> serverNodes = Lists.newArrayList(SERVER_NODES);
    synchronized (this) {
      int value = random.nextInt();
      if (value < 0) {
        value = Math.abs(value);
      }
      value = value % serverNodes.size();
      return serverNodes.get(value);
    }
  }

  @Override
  public void registry(int rpcPort) {
    CuratorFramework curatorFramework = ZkClientFacotry.getZkClient();
    String path = ZKPaths.makePath(SERVERADDRESS_NODEPATH,
        NetUtil.getLocalIp() + ":" + Integer.valueOf(rpcPort).toString());
    try {
      curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path);
    } catch (Exception e) {
      throw new DtsException(e);
    }
  }

  public static class ZkClientFacotry {

    private static Map<String, CuratorFramework> cacheConnection = Maps.newConcurrentMap();

    public static CuratorFramework getZkClient() {
      String zkConnection = System.getProperty("ZK_CONNECTION");
      if (cacheConnection.get(zkConnection) != null) {
        return cacheConnection.get(zkConnection);
      } else {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        CuratorFramework client =
            builder.connectString(zkConnection).sessionTimeoutMs(30000).connectionTimeoutMs(30000)
                .canBeReadOnly(true).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();
        try {
          client.blockUntilConnected();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return client;
      }
    }
  }


}
