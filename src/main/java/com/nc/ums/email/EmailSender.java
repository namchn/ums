package com.nc.ums.email;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class EmailSender {

    /**
     * 실제 메일 발송 수행.
     * - 성공: 메서드 정상 종료 (return void)
     * - 실패:
     *    - SMTP/메일 API가 4xx/5xx 등 실패 응답을 줬다면 SmtpSendException(코드 포함)을 던진다.
     *    - 네트워크/IO/타임아웃 등으로 실패하면 SmtpSendException으로 래핑하여 던진다.
     *
     * NOTE: 이 메서드는 '메일서버가 수신을 확인한 시점(2xx 응답)'을 성공으로 간주한다.
     */
    public void send(String payload) {
        // --- 예시: 실제 SMTP 클라이언트 또는 외부 API 호출 코드가 여기에 위치 ---
        // 아래는 예시 시뮬레이션 코드입니다. 실제 환경에서는 JavaMailSender/SMTP 라이브러리나 HTTP 클라이언트를 사용하세요.

        try {
            // 예시: payload에 "forceFailSMTP":true 가 있으면 SMTP 응답 실패 시뮬
            if (payload != null && payload.contains("\"forceFailSMTP\":true")) {
                // SMTP가 550 등의 오류를 준 상황(예시)
                throw new SmtpSendException("SMTP rejected message (simulated)", 550);
            }
            // 예시: payload에 "forceNetworkFail":true 가 있으면 네트워크 예외 시뮬
            if (payload != null && payload.contains("\"forceNetworkFail\":true")) {
                throw new IOException("simulated network error");
            }

            // 실제로는:
            // - JavaMailSender.send(mimeMessage) 같은 호출을 수행
            // - 호출 결과(예: SMTP 프로토콜 리턴코드)가 2xx가 아니면 SmtpSendException throw

            // 성공이면 아무일도 하지 않고 정상 반환
            return;
        } catch (IOException io) {
            // 네트워크/IO 문제는 SEND_ERROR로 분류
            throw new SmtpSendException("Network/IO error while sending email: " + io.getMessage(), io);
        }
    }
}