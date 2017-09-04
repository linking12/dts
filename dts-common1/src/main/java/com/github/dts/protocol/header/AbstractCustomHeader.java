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
package com.github.dts.protocol.header;

import com.github.dts.remoting.CommandCustomHeader;
import com.github.dts.remoting.exception.RemotingCommandException;

/**
 * @author liushiming
 * @version AbstractCommandCustomHeader.java, v 0.0.1 2017年9月4日 下午2:13:03 liushiming
 */
public abstract class AbstractCustomHeader implements CommandCustomHeader {

  private String msg;

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public void checkFields() throws RemotingCommandException {

  }
}
