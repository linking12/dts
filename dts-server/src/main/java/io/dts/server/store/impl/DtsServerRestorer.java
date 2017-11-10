package io.dts.server.store.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class DtsServerRestorer {

  public static List<Long> restoredCommittingTransactions = new ArrayList<Long>();

}
