package com.quancheng.dts.common;

public class DtsException extends RuntimeException {
  private static final long serialVersionUID = 5531074229174745826L;
  private int result;

  public DtsException() {}

  public DtsException(int result, String msg) {
    super(msg);
    this.setResult(result);
  }

  public DtsException(String msg) {
    this(ResultCode.SYSTEMERROR.getValue(), msg);
  }

  public DtsException(Throwable th) {
    super(th);
    this.result = ResultCode.SYSTEMERROR.getValue();
  }

  public DtsException(Throwable th, String msg) {
    super(msg, th);
    this.result = ResultCode.SYSTEMERROR.getValue();
  }

  public int getResult() {
    return result;
  }

  public void setResult(int result) {
    this.result = result;
  }
}
