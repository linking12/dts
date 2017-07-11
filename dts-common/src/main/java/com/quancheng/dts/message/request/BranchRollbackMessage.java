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

import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version BranchRollbackMessage.java, v 0.0.1 2017年7月11日 下午1:45:18 liushiming
 * @since JDK 1.8
 */
public class BranchRollbackMessage extends DtsMessage {

  private static final long serialVersionUID = -9078676714891161249L;

  private String serverAddr;

  private long tranId;

  private long branchId;

  private String appName;

  private String dbName;

  private byte commitMode;

  private byte isDelLock;

  private String udata;

  private ByteBuffer byteBuffer;

  public BranchRollbackMessage() {
    this(1 * 1024 * 1024);
  }

  public BranchRollbackMessage(int size) {
    super();
    this.byteBuffer = ByteBuffer.allocate(size);
  }

  public String getServerAddr() {
    return serverAddr;
  }


  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }


  public long getTranId() {
    return tranId;
  }


  public void setTranId(long tranId) {
    this.tranId = tranId;
  }


  public long getBranchId() {
    return branchId;
  }


  public void setBranchId(long branchId) {
    this.branchId = branchId;
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


  public byte getCommitMode() {
    return commitMode;
  }


  public void setCommitMode(byte commitMode) {
    this.commitMode = commitMode;
  }


  public byte getIsDelLock() {
    return isDelLock;
  }


  public void setIsDelLock(byte isDelLock) {
    this.isDelLock = isDelLock;
  }


  public String getUdata() {
    return udata;
  }

  public void setUdata(String udata) {
    this.udata = udata;
  }


  public ByteBuffer getByteBuffer() {
    return byteBuffer;
  }

  
  public void setByteBuffer(ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BRANCH_ROLLBACK;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    byteBuffer.putLong(this.tranId);
    byteBuffer.putLong(this.branchId);
    byteBuffer.put(this.commitMode);
    byteBuffer.put(this.isDelLock);
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

    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public boolean decode(ByteBuf in) {
    int i = in.readableBytes();
    if (i < 26)
      return false;
    this.tranId = in.readLong();
    this.branchId = in.readLong();
    this.commitMode = in.readByte();
    this.isDelLock = in.readByte();

    short len = in.readShort();
    i -= 26;
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

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setUdata(new String(bs, UTF8));
    }

    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BranchRollbackMessage [serverAddr=" + serverAddr + ", tranId=" + tranId + ", branchId="
        + branchId + ", appName=" + appName + ", dbName=" + dbName + ", commitMode=" + commitMode
        + ", isDelLock=" + isDelLock + ", udata=" + udata + ", byteBuffer=" + byteBuffer + "]";
  }

}
