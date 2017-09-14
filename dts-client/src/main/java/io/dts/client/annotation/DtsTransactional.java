package io.dts.client.annotation;

import org.springframework.core.annotation.AliasFor;

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
   * Alias for {@link #transactionManager}.
   * @see #transactionManager
   */
  @AliasFor("transactionManager")
  String value() default "";

  /**
   * A <em>qualifier</em> value for the specified transaction.
   * <p>May be used to determine the target transaction manager,
   * matching the qualifier value (or the bean name) of a specific
   * {@link org.springframework.transaction.PlatformTransactionManager}
   * bean definition.
   * @since 4.2
   * @see #value
   */
  @AliasFor("value")
  String transactionManager() default "";

  /**
   * millio second of timeout
   * @return
   */
  int timeout() default 3000;

  /**
   * millio second of effectiveTime
   * @return
   */
  int effectiveTime() default 3000;

//  DtsTranModel tranModel() default RB;

}
