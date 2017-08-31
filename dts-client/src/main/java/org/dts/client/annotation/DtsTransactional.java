package org.dts.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by guoyubo on 2017/8/31.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DtsTransactional {

  /**
   * The name of transactional
   * @return
   */
  String name() default "";

  /**
   * A <em>qualifier</em> value for the specified transaction.
   * <p>May be used to determine the target transaction manager,
   * matching the qualifier value (or the bean name) of a specific
   * {@link org.dts.client.DtsTransactionManager}
   * bean definition.
   * @since 4.2
   */
  String transactionManager() default "";


}
