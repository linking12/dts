package org.dts.server.processor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guoyubo on 2017/8/31.
 */
public class ProcessorFactory {

  private Map<Integer, BusinessProcessor> processorMap = new HashMap<>();

  public void setProcessor(Integer requestCode, BusinessProcessor businessProcessor) {
    processorMap.put(requestCode, businessProcessor);
  }

  public BusinessProcessor getProcessor(Integer requestCode) {
    return processorMap.get(requestCode);
  }


}
