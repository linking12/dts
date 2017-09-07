package com.quancheng.dts.event.eventbus;

public class SyncEventBus implements EventBus {

  private final com.google.common.eventbus.EventBus eventBus;

  public SyncEventBus() {
    this.eventBus = new com.google.common.eventbus.EventBus();
  }

  @Override
  public <T> void register(EventListener<T> listener) {
    this.eventBus.register(listener);
  }

  @Override
  public <T> void post(T event) {
    this.eventBus.post(event);
  }

}
