package com.quancheng.dts.event.eventbus;

import javax.annotation.PostConstruct;

/**
 * Usage:
 * <p>
 * firstly, set event bus by calling {@link #setEventBus(EventBus)} or constructor {@link #EventListener(EventBus)
 * secondly, register the event listener into event bus by calling {@link EventListener#init()}
 * <p>
 * If you do not set the event bus, the event listener will be registered on the default aysnc event
 * bus {@link EventBusFactory#getDefaultAsyncEventBus() }
 *
 * @author wangzheng
 */
public abstract class EventListener<T> {
  private EventBus eventBus;

  public EventListener(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public EventListener() {
    eventBus = EventBusFactory.getDefaultAsyncEventBus();
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @PostConstruct
  public void init() {
    eventBus.register(this);
  }

  public abstract void listen(T message);
}
