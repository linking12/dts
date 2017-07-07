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
package com.quancheng.dts.message.request;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version BranchCommitMessage.java, v 0.0.1 2017年7月6日 上午11:34:21 liushiming
 * @since JDK 1.8
 */
public class BranchCommitMessage extends DtsMessage {

  private static final long serialVersionUID = 1610151723761583054L;

  private String serverAddr;

  private List<Long> tranIds;

  private List<Long> branchIds;

  private String clientIp;

  private String appName;

  private String dbName;

  private String retrySql;

  private byte commitMode;

  private String udata;

  private ByteBuffer byteBuffer;

  public BranchCommitMessage() {
    this(2 * 1024 * 1024);
  }

  public BranchCommitMessage(int size) {
    super();
    this.tranIds = new ArrayList<Long>();
    this.branchIds = new ArrayList<Long>();
    this.byteBuffer = ByteBuffer.allocate(size);
  }

  public String getServerAddr() {
    return serverAddr;
  }


  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }


  public List<Long> getTranIds() {
    return tranIds;
  }


  public void setTranIds(List<Long> tranIds) {
    this.tranIds = tranIds;
  }


  public List<Long> getBranchIds() {
    return branchIds;
  }


  public void setBranchIds(List<Long> branchIds) {
    this.branchIds = branchIds;
  }


  public String getClientIp() {
    return clientIp;
  }


  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }


  public String getAppName() {
    return appName;
  }


  public void setAppName(String appName) {
    this.appName = appName;
  }


  public String getDbName() {
    return dbName;
  }


  public void setDbName(String dbName) {
    this.dbName = dbName;
  }


  public String getRetrySql() {
    return retrySql;
  }


  public void setRetrySql(String retrySql) {
    this.retrySql = retrySql;
  }


  public byte getCommitMode() {
    return commitMode;
  }

  public void setCommitMode(byte commitMode) {
    this.commitMode = commitMode;
  }

  public String getUdata() {
    return udata;
  }


  public void setUdata(String udata) {
    this.udata = udata;
  }


  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BRANCH_COMMIT;
  }


  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    byteBuffer.putInt(tranIds.size());
    for (int i = 0; i < tranIds.size(); i++) {
      byteBuffer.putLong(tranIds.get(i));
      byteBuffer.putLong(branchIds.get(i));
    }
    byteBuffer.put(this.commitMode);
    if (this.serverAddr != null) {
      byte[] bs = serverAddr.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.appName != null) {
      byte[] bs = appName.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.dbName != null) {
      byte[] bs = dbName.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.udata != null) {
      byte[] bs = udata.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.retrySql != null) {
      byte[] bs = retrySql.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public boolean decode(ByteBuf in) {
    int i = in.readableBytes();
    if (i < 15)
      return false;
    else
      i -= 15;

    int size = in.readInt();
    if (i < 16 * size)
      return false;
    else
      i -= (16 * size);

    for (int idx = 0; idx < size; idx++) {
      this.tranIds.add(in.readLong());
      this.branchIds.add(in.readLong());
    }
    this.commitMode = in.readByte();

    short len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setServerAddr(new String(bs, UTF8));
    }

    len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setAppName(new String(bs, UTF8));
    }

    len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setDbName(new String(bs, UTF8));
    }

    len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setUdata(new String(bs, UTF8));
    }

    len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setRetrySql(new String(bs, UTF8));
    }
    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BranchCommitMessage [serverAddr=" + serverAddr + ", tranIds=" + tranIds + ", branchIds="
        + branchIds + ", clientIp=" + clientIp + ", appName=" + appName + ", dbName=" + dbName
        + ", retrySql=" + retrySql + ", commitMode=" + commitMode + ", udata=" + udata
        + ", byteBuffer=" + byteBuffer + "]";
  }


}
