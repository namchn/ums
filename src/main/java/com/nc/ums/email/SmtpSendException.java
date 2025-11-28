package com.nc.ums.email;

//간단한 커스텀 예외
public class SmtpSendException extends RuntimeException {
	 private final int smtpCode; // 외부 API일 땐 -1 이나 HTTP 코드로 사용 가능
	
	 public SmtpSendException(String message) {
	     super(message);
	     this.smtpCode = -1;
	 }
	
	 public SmtpSendException(String message, int smtpCode) {
	     super(message);
	     this.smtpCode = smtpCode;
	 }
	
	 public SmtpSendException(String message, Throwable cause) {
	     super(message, cause);
	     this.smtpCode = -1;
	 }
	
	 public int getSmtpCode() { return smtpCode; }
}
