package org.dts.client.interceptor;

import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import lombok.Data;

/**
 * Created by guoyubo on 2017/9/5.
 */
@Data
public class DtsTransactionAttribute extends DefaultTransactionAttribute {


  private String tranModel;

  private int effectiveTime;

}
