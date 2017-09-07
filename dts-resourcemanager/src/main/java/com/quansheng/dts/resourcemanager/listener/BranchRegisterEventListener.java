package com.quansheng.dts.resourcemanager.listener;

import com.google.common.eventbus.Subscribe;
import com.quancheng.dts.event.eventbus.EventListener;
import com.quancheng.dts.event.message.BranchRegisterEvent;
import com.quansheng.dts.resourcemanager.DtsResourceManager;

/**
 * Created by guoyubo on 2017/9/6.
 */
public class BranchRegisterEventListener extends EventListener<BranchRegisterEvent> {

  private DtsResourceManager resourceManager;

  @Subscribe
  public void handle(final BranchRegisterEvent message) {
    long register = resourceManager.register(message.getKey(), message.getCommitMode());
  }

  @Override
  public void listen(final BranchRegisterEvent message) {

  }
}
