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
package io.dts.server.network.channel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.dts.common.util.NetUtil;
import io.dts.remoting.common.RemotingHelper;
import io.dts.remoting.common.RemotingUtil;
import io.netty.channel.Channel;

/**
 * @author liushiming
 * @version ChannelRepository.java, v 0.0.1 2017年9月6日 上午10:12:58 liushiming
 */
@Component
public class ChannelRepository {

  private static final Logger log = LoggerFactory.getLogger(ChannelRepository.class);

  private static final long LockTimeoutMillis = 3000;

  private static final long ChannelExpiredTimeout = 1000 * 120;

  private final Lock groupChannelLock = new ReentrantLock();

  private final HashMap<String, HashMap<Channel, ChannelInfo>> groupChannelTable =
      new HashMap<String, HashMap<Channel, ChannelInfo>>();

  public HashMap<String, HashMap<Channel, ChannelInfo>> getGroupChannelTable() {
    return groupChannelTable;
  }

  public void scanNotActiveChannel() {
    try {
      if (this.groupChannelLock.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
        try {
          for (final Map.Entry<String, HashMap<Channel, ChannelInfo>> entry : this.groupChannelTable
              .entrySet()) {
            final String group = entry.getKey();
            final HashMap<Channel, ChannelInfo> chlMap = entry.getValue();
            Iterator<Map.Entry<Channel, ChannelInfo>> it = chlMap.entrySet().iterator();
            while (it.hasNext()) {
              Map.Entry<Channel, ChannelInfo> item = it.next();
              final ChannelInfo info = item.getValue();
              long diff = System.currentTimeMillis() - info.getLastUpdateTimestamp();
              if (diff > ChannelExpiredTimeout) {
                it.remove();
                log.warn(
                    "SCAN: remove expired channel[{}] from ClientManager groupChannelTable, producer group name: {}",
                    RemotingHelper.parseChannelRemoteAddr(info.getChannel()), group);
                RemotingUtil.closeChannel(info.getChannel());
              }
            }
          }
        } finally {
          this.groupChannelLock.unlock();
        }
      } else {
        log.warn("ClientManager scanNotActiveChannel lock timeout");
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }


  public void doChannelCloseEvent(final String remoteAddr, final Channel channel) {
    if (channel != null) {
      try {
        if (this.groupChannelLock.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
          try {
            for (final Map.Entry<String, HashMap<Channel, ChannelInfo>> entry : this.groupChannelTable
                .entrySet()) {
              final String group = entry.getKey();
              final HashMap<Channel, ChannelInfo> clientChannelInfoTable = entry.getValue();
              final ChannelInfo clientChannelInfo = clientChannelInfoTable.remove(channel);
              if (clientChannelInfo != null) {
                log.info(
                    "NETTY EVENT: remove channel[{}][{}] from ClientManager groupChannelTable, producer group: {}",
                    clientChannelInfo.toString(), remoteAddr, group);
              }
            }
          } finally {
            this.groupChannelLock.unlock();
          }
        } else {
          log.warn("ClientManager doChannelCloseEvent lock timeout");
        }
      } catch (InterruptedException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public Channel getChannelByAddress(String address) {
    for (final Map.Entry<String, HashMap<Channel, ChannelInfo>> entry : this.groupChannelTable
        .entrySet()) {
      final HashMap<Channel, ChannelInfo> chlMap = entry.getValue();
      Iterator<Map.Entry<Channel, ChannelInfo>> it = chlMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Channel, ChannelInfo> item = it.next();
        final Channel channel = item.getKey();
        final String clientIp = NetUtil.toStringAddress(channel.remoteAddress());
        if (clientIp.equals(address)) {
          return channel;
        }
      }
    }
    return null;

  }


  public void registerChannel(final String group, final ChannelInfo clientChannelInfo) {
    try {
      ChannelInfo clientChannelInfoFound = null;
      if (this.groupChannelLock.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
        try {
          HashMap<Channel, ChannelInfo> channelTable = this.groupChannelTable.get(group);
          if (null == channelTable) {
            channelTable = new HashMap<Channel, ChannelInfo>();
            this.groupChannelTable.put(group, channelTable);
          }

          clientChannelInfoFound = channelTable.get(clientChannelInfo.getChannel());
          if (null == clientChannelInfoFound) {
            channelTable.put(clientChannelInfo.getChannel(), clientChannelInfo);
            log.info("new producer connected, group: {} channel: {}", group,
                clientChannelInfo.toString());
          }
        } finally {
          this.groupChannelLock.unlock();
        }

        if (clientChannelInfoFound != null) {
          clientChannelInfoFound.setLastUpdateTimestamp(System.currentTimeMillis());
        }
      } else {
        log.warn("ProducerManager registerProducer lock timeout");
      }
    } catch (InterruptedException e) {
      log.error("", e);
    }
  }


  public void unregisterChannel(final String group, final ChannelInfo clientChannelInfo) {
    try {
      if (this.groupChannelLock.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
        try {
          HashMap<Channel, ChannelInfo> channelTable = this.groupChannelTable.get(group);
          if (null != channelTable && !channelTable.isEmpty()) {
            ChannelInfo old = channelTable.remove(clientChannelInfo.getChannel());
            if (old != null) {
              log.info("unregister a producer[{}] from groupChannelTable {}", group,
                  clientChannelInfo.toString());
            }
            if (channelTable.isEmpty()) {
              this.groupChannelTable.remove(group);
              log.info("unregister a producer group[{}] from groupChannelTable", group);
            }
          }
        } finally {
          this.groupChannelLock.unlock();
        }
      } else {
        log.warn("ProducerManager unregisterProducer lock timeout");
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }
}
