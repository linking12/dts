package io.dts.client.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.client.aop.annotation.DtsModel;
import io.dts.client.aop.annotation.DtsTransaction;
import io.dts.client.template.TxcCallback;
import io.dts.client.template.TxcTransactionTemplate;

public class TransactionDtsInterceptor implements MethodInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(TransactionDtsInterceptor.class);
  private final TxcTransactionTemplate template = new TxcTransactionTemplate();
  private volatile HashMap<Object, DtsTransaction> methodMap =
      new HashMap<Object, DtsTransaction>();

  public TransactionDtsInterceptor(List<MethodDesc> list) {
    for (MethodDesc md : list) {
      logger.info("add method " + md.getM().getName());
      methodMap.put(formatMethod(md.getM()), md.getTrasactionAnnotation());
    }
  }

  @Override
  public Object invoke(final MethodInvocation arg0) throws Throwable {
    DtsTransaction txc = getTxcTransaction(arg0);
    if (txc != NULL) {
      if (txc.type() == DtsModel.RT) {
        return template.runRT(new TxcCallback() {
          @Override
          public Object callback() throws Throwable {
            return arg0.proceed();
          }
        }, txc.effectiveTime());
      } else if (txc.type() == DtsModel.ATMT) {
        return template.runATMT(new TxcCallback() {
          @Override
          public Object callback() throws Throwable {
            return arg0.proceed();
          }
        }, txc.timeout());
      }
    }
    return arg0.proceed();
  }

  protected final static DtsTransaction NULL = new DtsTransaction() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public int timeout() {
      return 0;
    }

    @Override
    public DtsModel type() {
      return null;
    }

    @Override
    public int effectiveTime() {
      return 0;
    }
  };

  private static String formatMethod(Method method) {
    StringBuilder sb = new StringBuilder();

    String mehodName = method.getName();
    Class<?>[] params = method.getParameterTypes();
    sb.append(mehodName);
    sb.append("(");

    int paramPos = 0;
    for (Class<?> claz : params) {
      sb.append(claz.getName());
      if (++paramPos < params.length) {
        sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  protected DtsTransaction getTxcTransaction(MethodInvocation arg0) {
    DtsTransaction txc = methodMap.get(arg0.getMethod());
    if (txc == null) {
      synchronized (this) {
        txc = methodMap.get(arg0.getMethod());
        if (txc == null) {
          String methodStringDesc = formatMethod(arg0.getMethod());
          txc = methodMap.get(methodStringDesc);
          if (txc == null) {
            txc = NULL;
          }
          HashMap<Object, DtsTransaction> newMap = new HashMap<Object, DtsTransaction>();
          newMap.putAll(methodMap);
          newMap.remove(methodStringDesc);
          newMap.put(arg0.getMethod(), txc);
          methodMap = newMap;
        }
      }
    }
    return txc;
  }
}
