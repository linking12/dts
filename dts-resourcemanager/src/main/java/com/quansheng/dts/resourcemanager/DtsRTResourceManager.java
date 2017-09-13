package com.quansheng.dts.resourcemanager;

import com.quancheng.dts.message.response.BeginRetryBranchResultMessage;

import javax.sql.DataSource;

/**
 * Created by guoyubo on 2017/9/7.
 */
public interface DtsRTResourceManager extends DtsResourceManager {

  public BeginRetryBranchResultMessage beginRtBranch(DataSource dataSource, String sql);


}
