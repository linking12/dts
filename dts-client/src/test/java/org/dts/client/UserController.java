package org.dts.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by guoyubo on 2017/9/5.
 */
@RestController
public class UserController {

  @Autowired
  UserService userService;

  @RequestMapping(value = "/user", method = RequestMethod.GET)
  public String createUser() {
    userService.createUser();
    return "ok";
  }

  @RequestMapping(value = "/userRt", method = RequestMethod.GET)
  public String rtCreateUser() {
    userService.rtCreateUser();
    return "ok";
  }

}
