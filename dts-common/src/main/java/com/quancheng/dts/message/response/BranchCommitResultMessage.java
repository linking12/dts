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
package com.quancheng.dts.message.response;

import java.util.ArrayList;
import java.util.List;

import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author liushiming
 * @version BranchCommitResultMessage.java, v 0.0.1 2017年7月5日 下午5:37:49 liushiming
 * @since JDK 1.8
 */
public class BranchCommitResultMessage extends ResultMessage {

  private static final long serialVersionUID = 3709130876007245975L;

  private List<Long> tranIds;

  private List<Long> branchIds;

  public BranchCommitResultMessage() {
    this(1 * 1024 * 1024);
  }

  public BranchCommitResultMessage(int size) {
    super(size);
    this.tranIds = new ArrayList<Long>();
    this.branchIds = new ArrayList<Long>();
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


  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BRANCH_COMMIT_RESULT;
  }

  /**
   * @see com.quancheng.dts.message.DtsMessage#handleMessage(long, java.lang.String,
   *      java.lang.String, java.lang.String, com.quancheng.dts.message.DtsMessage,
   *      com.quancheng.dts.message.response.ResultMessage[], int)
   */
  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMessage message, ResultMessage[] results, int idx) {
    super.getHandler().handleMessage(msgId, dbKeys, clientIp, clientAppName, this, results, idx);
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    super.encode();
    byteBuffer.putInt(tranIds.size());
    for (int i = 0; i < tranIds.size(); i++) {
      byteBuffer.putLong(tranIds.get(i));
      byteBuffer.putLong(branchIds.get(i));
    }

    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#decode(ByteBuf in)
   */
  @Override
  public boolean decode(ByteBuf in) {
    if (!super.decode(in))
      return false;

    int i = in.readableBytes();
    if (i < 4)
      return false;
    else
      i -= 4;

    int size = in.readInt();
    if (i < 16 * size)
      return false;
    else
      i -= (16 * size);

    for (int idx = 0; idx < size; idx++) {
      this.tranIds.add(in.readLong());
      this.branchIds.add(in.readLong());
    }
    return true;
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BranchCommitResultMessage [tranIds=" + tranIds + ", branchIds=" + branchIds + ", "
        + super.toString() + "]";
  }

  public static void main(String[] args) {
    BranchCommitResultMessage commitMessage = new BranchCommitResultMessage();
    List<Long> tranIds = new ArrayList<Long>();
    List<Long> branchIds = new ArrayList<Long>();
    tranIds.add(1L);
    branchIds.add(2L);
    commitMessage.setTranIds(tranIds);
    commitMessage.setBranchIds(branchIds);
    byte[] bytemessage = commitMessage.encode();
    BranchCommitResultMessage commitMessageDecode = new BranchCommitResultMessage();
    ByteBuf buf = Unpooled.buffer(2 * 1024 * 1024);
    buf.writeBytes(bytemessage);
    commitMessageDecode.decode(buf);
    System.out.println(commitMessageDecode);
  }

}
