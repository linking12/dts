package com.quancheng.dts.event.eventbus;

public interface EventBus {

  /**
   * Register an event listener.
   *
   * @param listener
   */
  <T> void register(EventListener<T> listener);

  /**
   * Trigger a event.
   *
   * @param event
   */
  <K> void post(K event);

}
