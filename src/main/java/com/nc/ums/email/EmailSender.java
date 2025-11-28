package com.nc.ums.email;

public interface EmailSender {  //단위/통합 테스트에서 외부 의존성 격리 가능.
	void send(String payload) throws SmtpSendException;
}
