package io.dts.resourcemanager.help;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import io.dts.resourcemanager.struct.TxcIsolation;

/**
 * @author qinan.qn@taobao.com 2014年11月17日
 */
public class TXCIsolationFactoryBean extends AbstractFactoryBean {

  private TxcIsolation iso;

  /**
   * @param iso
   */
  public TXCIsolationFactoryBean(TxcIsolation iso) {
    super();
    this.iso = iso;
  }

  @Override
  public Class<TxcIsolation> getObjectType() {
    return TxcIsolation.class;
  }

  @Override
  protected Object createInstance() throws Exception {
    return iso;
  }

}
