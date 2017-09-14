package io.dts.common.serviceloader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LoadLevel
 * <p />
 * 对于可以被框架中自动激活加载扩展，此Annotation用于配置扩展被自动激活加载条件。比如，有多个实现可以定义加载优先级
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface LoadLevel {

	String name(); // 可以为每个组件定义一个name，通过load的时候指定name即可匹配

	int order();

}
