package io.dts.common.protocol;

/**
 * Created by cn40387 on 15/3/5.
 */
public class RequestCode {
  public static final int TYPE_BEGIN = 1;

  public static final int TYPE_BRANCH_COMMIT = 3;

  public static final int TYPE_BRANCH_ROLLBACK = 5;

  public static final int TYPE_GLOBAL_COMMIT = 7;

  public static final int TYPE_GLOBAL_ROLLBACK = 9;

  public static final int TYPE_REGIST = 11;

  public static final int TYPE_REPORT_STATUS = 13;

  public static final int TYPE_BEGIN_RETRY_BRANCH = 15;

  public static final int TYPE_REPORT_UDATA = 17;

  public static final int TYPE_DTS_MERGE = 19;

  public static final int TYPE_QUERY_LOCK = 21;

  public static final int TYPE_REG_CLT = 101;

  public static final int TYPE_REG_RM = 103;

  public static final int TYPE_REG_CLUSTER_NODE = 105;

  public static final int TYPE_CLUSTER_BRANCH = 107;

  public static final int TYPE_CLUSTER_GLOBAL = 109;

  public static final int TYPE_CLUSTER_SYNC = 111;

  public static final int TYPE_CLUSTER_DUMP = 113;

  public static final int TYPE_CLUSTER_MERGE = 115;

  public static final int HEART_BEAT = 400;
}
