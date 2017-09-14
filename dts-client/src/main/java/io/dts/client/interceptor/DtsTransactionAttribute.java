package io.dts.client.interceptor;

import org.springframework.transaction.interceptor.DefaultTransactionAttribute;


/**
 * Created by guoyubo on 2017/9/5.
 */
public class DtsTransactionAttribute extends DefaultTransactionAttribute {


  private String tranModel;

  private int effectiveTime;

  public String getTranModel() {
    return tranModel;
  }

  public void setTranModel(final String tranModel) {
    this.tranModel = tranModel;
  }

  public int getEffectiveTime() {
    return effectiveTime;
  }

  public void setEffectiveTime(final int effectiveTime) {
    this.effectiveTime = effectiveTime;
  }
}
