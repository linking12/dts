package org.dts.client;

import org.dts.client.annotation.DtsTransactional;
import org.springframework.stereotype.Service;

import com.quancheng.dts.common.DtsTranModel;

/**
 * Created by guoyubo on 2017/9/5.
 */
@Service
public class UserService {

  @DtsTransactional
  public void createUser() {
    System.out.println("ok");
  }


  @DtsTransactional(tranModel = DtsTranModel.RT)
  public void rtCreateUser() {
    System.out.println("ok");
  }



}
