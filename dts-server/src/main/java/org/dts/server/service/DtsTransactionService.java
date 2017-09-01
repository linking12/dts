package org.dts.server.service;

import org.dts.server.entity.DtsTransaction;
import org.dts.server.repository.DtsTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.message.request.BeginMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by guoyubo on 2017/9/1.
 */
@Service
public class DtsTransactionService {

  @Autowired
  DtsTransactionRepository transactionRepository;

  @Autowired
  KeyGenerator keyGenerator;

  @Transactional
  public DtsTransaction createTransaction(BeginMessage beginMessage,
      final HashMap<String, String> extFields) {
    DtsTransaction dtsTransaction = new DtsTransaction();
    dtsTransaction.setId(keyGenerator.generateKey().longValue());
    dtsTransaction.setAppName(extFields.get("appName"));
    dtsTransaction.setServerGroup(extFields.get("serverGroup"));
    dtsTransaction.setAppAddress(extFields.get("appAddress"));
    dtsTransaction.setStartAt(new Date());
    dtsTransaction.setStatus(0);
    transactionRepository.insert(dtsTransaction);
    return dtsTransaction;

  }

}
