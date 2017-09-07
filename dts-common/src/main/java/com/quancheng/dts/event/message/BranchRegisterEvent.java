package com.quancheng.dts.event.message;

import com.quancheng.dts.common.CommitMode;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by guoyubo on 2017/9/6.
 */
@Data
@AllArgsConstructor
public class BranchRegisterEvent {

  private String key;
  private CommitMode commitMode;
}
