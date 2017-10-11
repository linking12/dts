package io.dts.client.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface DtsTransaction {
  /**
   * 仅对type=TxcModel.ATMT有效，此值定义了一个全局事务最大的生存周期， 超过此值后，全局事务将被回滚。 值等于0时，代表事务永不超时 默认值为60000ms
   * 注意：在设定此值时，请评估，超时时间是否过短或过长，过短将可能造成事务失败，过长可能造成事务失败时，脏读的可能性
   * 
   * @since 1.0.2
   */
  int timeout() default 60000;

  /**
   * TxcModel.ATMT txc 标准模式或手动模式 TxcModel.RT txc RT模式
   * 
   * @since 1.0.2
   */
  DtsModel type() default DtsModel.ATMT;

  /**
   * 仅对type=TxcModel.RT有效，txc保证在此时间内，会对开启了此模式下sql进行重试，在超过，此事件后， 对通过旺旺及邮件形式，进行告警通知 默认值为3600000ms
   * 
   * @since 1.0.2
   */
  int effectiveTime() default 3600000;
}
