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


  /**
   * @return the serverAddr
   */
  public String getServerAddr() {
    return serverAddr;
  }

  /**
   * @param serverAddr the serverAddr to set
   */
  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }

  /**
   * @return the tranId
   */
  public long getTranId() {
    return tranId;
  }

  /**
   * @param tranId the tranId to set
   */
  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  /**
   * @return the branchId
   */
  public long getBranchId() {
    return branchId;
  }

  /**
   * @param branchId the branchId to set
   */
  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }

  /**
   * @return the appName
   */
  public String getAppName() {
    return appName;
  }

  /**
   * @param appName the appName to set
   */
  public void setAppName(String appName) {
    this.appName = appName;
  }

  /**
   * @return the dbName
   */
  public String getDbName() {
    return dbName;
  }

  /**
   * @param dbName the dbName to set
   */
  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  /**
   * @return the commitMode
   */
  public byte getCommitMode() {
    return commitMode;
  }

  /**
   * @param commitMode the commitMode to set
   */
  public void setCommitMode(byte commitMode) {
    this.commitMode = commitMode;
  }

  /**
   * @return the isDelLock
   */
  public byte getIsDelLock() {
    return isDelLock;
  }

  /**
   * @param isDelLock the isDelLock to set
   */
  public void setIsDelLock(byte isDelLock) {
    this.isDelLock = isDelLock;
  }

  /**
   * @return the udata
   */
  public String getUdata() {
    return udata;
  }

  /**
   * @param udata the udata to set
   */
  public void setUdata(String udata) {
    this.udata = udata;
  }

  /**
   * @return the byteBuffer
   */
  public ByteBuffer getByteBuffer() {
    return byteBuffer;
  }

  /**
   * @param byteBuffer the byteBuffer to set
   */
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
