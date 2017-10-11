package io.dts.client.aop;

import java.lang.reflect.Method;

import io.dts.client.aop.annotation.DtsTransaction;

/**
 * @author qinan.qn@taobao.com 2014年9月23日
 */
public class MethodDesc {
  private DtsTransaction trasactionAnnotation;
  private Method m;

  /**
   * @param trasactionAnnotation
   * @param m
   */
  public MethodDesc(DtsTransaction trasactionAnnotation, Method m) {
    super();
    this.trasactionAnnotation = trasactionAnnotation;
    this.m = m;
  }

  public DtsTransaction getTrasactionAnnotation() {
    return trasactionAnnotation;
  }

  public void setTrasactionAnnotation(DtsTransaction trasactionAnnotation) {
    this.trasactionAnnotation = trasactionAnnotation;
  }

  public Method getM() {
    return m;
  }

  public void setM(Method m) {
    this.m = m;
  }

}
