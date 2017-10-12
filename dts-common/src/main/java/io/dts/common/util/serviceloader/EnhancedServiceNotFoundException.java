package io.dts.common.util.serviceloader;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class EnhancedServiceNotFoundException extends NestableRuntimeException {

	private static final long serialVersionUID = 7748438218914409019L;

	public EnhancedServiceNotFoundException(String errorCode) {
		super(errorCode);
	}

	public EnhancedServiceNotFoundException(String errorCode, Throwable cause) {
		super(errorCode, cause);
	}

	public EnhancedServiceNotFoundException(String errorCode, String errorDesc) {
		super(errorCode + ":" + errorDesc);
	}

	public EnhancedServiceNotFoundException(String errorCode, String errorDesc, Throwable cause) {
		super(errorCode + ":" + errorDesc, cause);
	}

	public EnhancedServiceNotFoundException(Throwable cause) {
		super(cause);
	}

	public Throwable fillInStackTrace() {
		return this;
	}

}