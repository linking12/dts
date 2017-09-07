package com.quancheng.dts.event.eventbus;

public class EventBusFactory {

  private EventBusFactory() {
  }

  private static class EventBusHolder {
    public static SyncEventBus syncEventBus = new SyncEventBus();
    public static AsyncEventBus asyncEventBus = new AsyncEventBus();
  }

  public static EventBus getDefaultSyncEventBus() {
    return EventBusHolder.syncEventBus;
  }

  public static EventBus getDefaultAsyncEventBus() {
    return EventBusHolder.asyncEventBus;
  }
}
