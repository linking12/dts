package io.dts.server.store.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jiangyu.jy
 * 
 *         TXC Server重启后的恢复工作
 */
@Repository
public class DtsServerRestorer {

  public static List<Long> restoredCommittingTransactions = new ArrayList<Long>();

}
