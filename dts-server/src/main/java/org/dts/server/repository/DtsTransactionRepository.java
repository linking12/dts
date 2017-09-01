package org.dts.server.repository;

import org.dts.server.entity.DtsTransaction;
import org.dts.server.mapper.DtsTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by guoyubo on 2017/9/1.
 */
@Component
public class DtsTransactionRepository {

  @Autowired
  private DtsTransactionMapper transactionMapper;

  public int insert(DtsTransaction dtsTransaction) {
    return transactionMapper.insert(dtsTransaction);
  }

  public  int update(Long id, Integer status, Date endAt) {
    return transactionMapper.update(id, status, endAt);
  }

  public DtsTransaction getById(Long id) {
    return transactionMapper.getById(id);
  }

}
