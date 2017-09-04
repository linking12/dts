/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.dts.protocol;

import com.github.dts.remoting.protocol.RemotingSysResponseCode;

public class ResponseCode extends RemotingSysResponseCode {

  public static final short TYPE_BEGIN_RESULT = 2;

  public static final short TYPE_BRANCH_COMMIT_RESULT = 4;

  public static final short TYPE_BRANCH_ROLLBACK_RESULT = 6;

  public static final short TYPE_GLOBAL_COMMIT_RESULT = 8;

  public static final short TYPE_GLOBAL_ROLLBACK_RESULT = 10;

  public static final short TYPE_REGIST_RESULT = 12;

  public static final short TYPE_REPORT_STATUS_RESULT = 14;

  public static final short TYPE_BEGIN_RETRY_BRANCH_RESULT = 16;

  public static final short TYPE_REPORT_UDATA_RESULT = 18;

  public static final short TYPE_DTS_MERGE_RESULT = 20;

  public static final short TYPE_QUERY_LOCK_RESULT = 22;

  public static final short TYPE_REG_CLT_RESULT = 102;

  public static final short TYPE_REG_RM_RESULT = 104;

  public static final short TYPE_REG_CLUSTER_NODE_RESULT = 106;

  public static final short TYPE_CLUSTER_BRANCH_RESULT = 108;

  public static final short TYPE_CLUSTER_GLOBAL_RESULT = 110;

  public static final short TYPE_CLUSTER_SYNC_RESULT = 112;

  public static final short TYPE_CLUSTER_DUMP_RESULT = 114;

  public static final short TYPE_CLUSTER_MERGE_RESULT = 116;

}
