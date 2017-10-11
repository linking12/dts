package io.dts.common.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TxcConstants {
  public static final long RPC_INVOKE_TIMEOUT = 30 * 1000;
  public final static String DIAMOND_GROUP = "TXC_GROUP";
  public static final String VERSION_1_0 = "1.0";
  public static final String VERSION_1_0_3 = "1.0.3";
  public final static String DIAMOND_MT_JOURNEL_GROUP = "TXC_MT_JOURNEL";
  public final static String DIAMOND_MT_JOURNEL_DATAID_PREFIX = "com.taobao.txc.mtjournel.";
  public final static String DIAMOND_GROUPLIST_DATAID_PREFIX = "com.taobao.txc.grouplist.";
  public final static String DIAMOND_SWITCHER_GLOBAL_DATAID = "com.taobao.txc.globalswitcher";
  public final static String DIAMOND_SWITCHER_WHITELIST_DATAID = "com.taobao.txc.whitelistswitcher";

  public static final AttributeKey<String> VERSIONKEY = AttributeKey.valueOf("VERSION");
  public static final Map<String, String> versionMap = new ConcurrentHashMap<String, String>();
  public static boolean SKIP_VER_CHK = true;

  public static void addChannelVersion(Channel c, String v) {
    versionMap.put(c.remoteAddress().toString(), v);
  }

  public static boolean isHandedChannel(Channel channel) {
    return versionMap.containsKey(channel.remoteAddress().toString());
  }

  public static boolean isVersion_1_0(Channel c) {
    return TxcConstants.VERSION_1_0.equalsIgnoreCase(versionMap.get(c.remoteAddress().toString()));
  }

  public static boolean isLargeThanVersion_1_0(Channel c) {
    if (SKIP_VER_CHK)
      return true;
    return (versionMap.get(c.remoteAddress().toString()).compareTo(TxcConstants.VERSION_1_0) > 0)
        ? true : false;
  }

  public static boolean isVersion_1_0_3(Channel c) {
    return TxcConstants.VERSION_1_0_3
        .equalsIgnoreCase(versionMap.get(c.remoteAddress().toString()));
  }

  public static void setVersion(ChannelHandlerContext ctx, String version) {
    ctx.attr(TxcConstants.VERSIONKEY).set(version);
  }

  public static boolean isVersion_1_0(ChannelHandlerContext ctx) {
    return TxcConstants.VERSION_1_0.equalsIgnoreCase(ctx.attr(VERSIONKEY).get());
  }

  public static boolean isVersion_1_0_3(ChannelHandlerContext ctx) {
    return TxcConstants.VERSION_1_0_3.equalsIgnoreCase(ctx.attr(VERSIONKEY).get());
  }

  public static boolean isVersion_1_0(String version) {
    return TxcConstants.VERSION_1_0.equalsIgnoreCase(version);
  }

  public static boolean isVersion_1_0_3(String version) {
    return TxcConstants.VERSION_1_0_3.equalsIgnoreCase(version);
  }
}
