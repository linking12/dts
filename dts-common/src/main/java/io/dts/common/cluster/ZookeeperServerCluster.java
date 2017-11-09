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
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.CachedAtomicInteger;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.dts.common.exception.DtsException;
import io.dts.common.util.NetUtil;

/**
 * @author liushiming
 * @version DefaultServerCluster.java, v 0.0.1 2017年10月17日 下午5:10:08 liushiming
 */
public class ZookeeperServerCluster implements ServerCluster, PathChildrenCacheListener {


  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServerCluster.class);

  private static ZookeeperServerCluster serverCluster = new ZookeeperServerCluster();

  private String ZOOKEEPER_ENV_URL = "ZK_CONNECTION";

  private List<String> clusters = Lists.newArrayList();

  private String dtsServerParentNode = "/dts/servers";

  private Random random = new Random(System.currentTimeMillis());

  private transient CuratorFramework client;

  private transient PathChildrenCache pathCache;

  private transient MIdGenerator midGenerator;

  private ZookeeperServerCluster() {
    String connectString = System.getenv(ZOOKEEPER_ENV_URL);
    if (StringUtils.isEmpty(connectString)) {
      connectString = System.getProperty(ZOOKEEPER_ENV_URL);
    }
    try {
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
      client = CuratorFrameworkFactory.builder().connectString(connectString)
          .retryPolicy(retryPolicy).connectionTimeoutMs(5000).build();
      client.start();
      initIdgenerator();
      initPathCache();

    } catch (Exception e) {
      LOGGER.error("ZKCfgSource Initial Exception, exception = {}", e);
    }
  }

  private void initIdgenerator() {
    DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(client,
        dtsServerParentNode, new ExponentialBackoffRetry(1000, 1));
    midGenerator = new MIdGenerator(atomicInteger, 1);
  }

  private void initPathCache() throws Exception {
    pathCache = new PathChildrenCache(client, dtsServerParentNode, true);
    pathCache.start();
    Executor executor = Executors.newFixedThreadPool(2);
    pathCache.getListenable().addListener(this, executor);
  }

  public static ServerCluster getInstance() {
    return serverCluster;
  }


  @Override
  public String select() {
    synchronized (this) {
      int value = random.nextInt();
      if (value < 0) {
        value = Math.abs(value);
      }
      value = value % clusters.size();
      return clusters.get(value);
    }
  }

  @Override
  public Integer registry(int rpcPort) {
    String path = ZKPaths.makePath(dtsServerParentNode,
        NetUtil.getLocalIp() + ":" + Integer.valueOf(rpcPort).toString());
    try {
      Stat stat = client.checkExists().forPath(path);
      if (stat != null) {
        client.delete().forPath(path);
      }
      Integer mid = midGenerator.next();
      client.create().withMode(CreateMode.EPHEMERAL).forPath(path, mid.toString().getBytes());
      return mid;
    } catch (Exception e) {
      throw new DtsException(e);
    }
  }


  @Override
  public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
    if (event.getData() == null) {
      return;
    }
    String path = event.getData().getPath();
    String clusterNode = StringUtils.replace(path, dtsServerParentNode + "/", "");
    switch (event.getType()) {
      case CHILD_UPDATED:
      case CHILD_ADDED:
      case INITIALIZED: {
        clusters.clear();
        clusters.add(clusterNode);
      }
        break;
      default:
        break;
    }

  }

  private static class MIdGenerator {
    private static final Logger log = LoggerFactory.getLogger(MIdGenerator.class);
    private final CachedAtomicInteger atomicInteger;

    public MIdGenerator(DistributedAtomicInteger atomicInteger, int cacheFactor) {
      this.atomicInteger = new CachedAtomicInteger(atomicInteger, cacheFactor);
    }

    public Integer next() {
      try {
        AtomicValue<Integer> code = atomicInteger.next();
        if (code.succeeded())
          return code.postValue();

        return -1;
      } catch (Exception ex) {
        if (log.isErrorEnabled()) {
          log.error("Cannot get the increment serial number from ZooKeeper", ex);
        }

        return -1;
      }
    }
  }

}
