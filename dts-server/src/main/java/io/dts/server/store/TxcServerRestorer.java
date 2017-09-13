package io.dts.server.store;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyu.jy
 * 
 *         TXC Server重启后的恢复工作
 */
public class TxcServerRestorer {

  public static List<Long> restoredCommittingTransactions = new ArrayList<Long>();

}
