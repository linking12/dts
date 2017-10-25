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
package io.dts.resourcemanager.struct;

/** 
 * @author liushiming 
 * @version UndoLogMode.java, v 0.0.1 2017年10月25日 下午6:31:03 liushiming 
 */
public enum UndoLogMode {
  /**
   * 正常日志
   */
  COMMON_LOG(0),

  /**
   * 错误日志
   */
  ERROR_LOG(1),

  /**
   * 已删除日志
   */
  DELETED_LOG(2),

  /**
   * rt journel for record servers that has been connected by rt-rm
   */
  RT_JOURNEL(3),

  /**
   * 数据库表Meta
   */
  TABLE_MATA(9001);

  private UndoLogMode(int value) {
      this.value = value;
  }

  public int getValue() {
      return value;
  }

  private int value;
}
