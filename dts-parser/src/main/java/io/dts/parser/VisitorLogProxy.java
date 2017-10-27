/*
 * Copyright 1999-2015 dangdang.com. <p> Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. </p>
 */

package io.dts.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.exception.DtsException;
import io.dts.parser.vistor.ITxcVisitor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


public final class VisitorLogProxy {
  private static final Logger log = LoggerFactory.getLogger(VisitorLogProxy.class);

  /**
   * 打印SQL解析调用树.
   * 
   * @param <T> 泛型
   * @param target 待增强类
   * @param sqlStatement
   * @param parameterSet
   * @return 增强后的新类的对象
   */
  @SuppressWarnings("unchecked")
  public static <T> T enhance(final Class<T> target, final DtsSQLStatement sqlStatement,
      final List<Object> parameterSet) {
    if (log.isTraceEnabled()) {
      Enhancer result = new Enhancer();
      result.setSuperclass(target);
      result.setCallback(new VisitorHandler());
      return (T) result.create(new Class[] {DtsSQLStatement.class, List.class},
          new Object[] {sqlStatement, parameterSet});
    } else {
      try {
        return target.getDeclaredConstructor(DtsSQLStatement.class, List.class)
            .newInstance(sqlStatement, parameterSet);
      } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
          | InvocationTargetException ex) {
        log.error("create Visitor exception: {}", ex);
        throw new DtsException(ex);
      }
    }
  }

  private static class VisitorHandler implements MethodInterceptor {

    private final StringBuilder hierarchyIndex = new StringBuilder();

    private Integer depth = 0;

    @Override
    public Object intercept(final Object enhancedObject, final Method method,
        final Object[] arguments, final MethodProxy methodProxy) throws Throwable {
      if (isPrintable(method)) {
        hierarchyIn();
        log.trace("{}visit node: {}", hierarchyIndex, arguments[0].getClass());
        log.trace("{}visit argument: {}", hierarchyIndex, arguments[0]);
      }
      Object result = methodProxy.invokeSuper(enhancedObject, arguments);
      if (isPrintable(method)) {
        ITxcVisitor visitor = (ITxcVisitor) enhancedObject;
        log.trace("{}endVisit node: {}", hierarchyIndex, arguments[0].getClass());
        log.trace("{}endVisit full sql: {}", hierarchyIndex, visitor.getFullSql());
        hierarchyOut();
      }
      return result;
    }

    private boolean isPrintable(final Method method) {
      return log.isTraceEnabled() && "visit".equals(method.getName());
    }

    private void hierarchyIn() {
      hierarchyIndex.append("  ").append(++depth).append(" ");
    }

    private void hierarchyOut() {
      hierarchyIndex.delete(hierarchyIndex.length() - 3 - depth.toString().length(),
          hierarchyIndex.length());
      depth--;
    }
  }
}
