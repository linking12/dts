package org.dts.server.entity;

import java.util.Date;

import lombok.Data;

/**
 * Created by guoyubo on 2017/9/1.
 */
@Data
public class DtsTransaction {

  private long id;

  private String appName;

  private String serverGroup;

  private String appAddress;

  private Integer status;

  private Date startAt;

  private Date endAt;



}
