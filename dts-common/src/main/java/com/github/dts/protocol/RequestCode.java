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

public class RequestCode {

  public static final short TYPE_BEGIN = 1;

  public static final short TYPE_BRANCH_COMMIT = 3;

  public static final short TYPE_BRANCH_ROLLBACK = 5;

  public static final short TYPE_GLOBAL_COMMIT = 7;

  public static final short TYPE_GLOBAL_ROLLBACK = 9;

  public static final short TYPE_REGIST = 11;

  public static final short TYPE_REPORT_STATUS = 13;

  public static final short TYPE_BEGIN_RETRY_BRANCH = 15;

  public static final short TYPE_REPORT_UDATA = 17;

  public static final short TYPE_DTS_MERGE = 19;

  public static final short TYPE_QUERY_LOCK = 21;

  public static final short TYPE_REG_CLT = 101;

  public static final short TYPE_REG_RM = 103;

  public static final short TYPE_REG_CLUSTER_NODE = 105;

  public static final short TYPE_CLUSTER_BRANCH = 107;

  public static final short TYPE_CLUSTER_GLOBAL = 109;

  public static final short TYPE_CLUSTER_SYNC = 111;

  public static final short TYPE_CLUSTER_DUMP = 113;

  public static final short TYPE_CLUSTER_MERGE = 115;

}
