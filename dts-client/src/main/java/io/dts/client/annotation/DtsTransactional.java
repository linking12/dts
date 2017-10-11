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


  @AliasFor("transactionManager")
  String value() default "";


  @AliasFor("value")
  String transactionManager() default "";


  int timeout() default 3000;


  int effectiveTime() default 3000;


}
