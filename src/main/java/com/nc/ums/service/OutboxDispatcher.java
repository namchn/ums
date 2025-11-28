package com.nc.ums.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.nc.ums.domain.Outbox;
import com.nc.ums.email.EmailSender;
import com.nc.ums.email.SmtpSendException;
import com.nc.ums.repo.OutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@EnableScheduling // 스케줄링 기능 활성화
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {

 private final OutboxRepository outboxRepo;
 private final OutboxProcessor outboxProcessor;
 //private final SmtpEmailSender emailSender;
 private final EmailSender emailSender;
 
 //injected executor bean
 @Qualifier("outboxTaskExecutor")
 private final ThreadPoolTaskExecutor outboxTaskExecutor;

 @Scheduled(fixedDelay = 5000)
 public void dispatch() {
     log.info("[OutboxDispatcher - dispatch]");
     List<Outbox> rows = outboxRepo.findByPublishedFalse();

     for (Outbox r : rows) {
         Long outboxId = r.getId();
         
         // '선점(claim)' 용도로 쓰는 lock-free 방식의 CAS(Compare-And-Swap)
     	 //CAS 방식으로 선점(optional: updateStatusIf) 하여 동시 처리 방지
     	 //int updated  = msgRepo.updateStatusIf(r.getAggregateId()  , "NEW", "PROCESSING");
         boolean claimed;
         try {
             claimed = outboxProcessor.claimMessage(r);
         } catch (Exception e) {
             log.error("claim failed for outboxId={}", outboxId, e);
             continue;
         }
         
         // 인스턴스간 경쟁시   선점 성공 여부를 분기
         if (!claimed) continue;// 선점 실패: 이미 PROCESSING/SUCCESS/FAILED 등 다른 상태

         
         /*
			 * 트랜잭션 밖에서 실발송 로직
			 * SMTP
			 * 사내 메일서버 API
			 * 외부 발송 시스템(API Gateway)
			 * 예)emailSender.send(outbox.getPayload());
			 * 
			 * 지연 처리가 필요할 경우 여기 로직 추가 (정책상의 속도 제한)
			 * 
		 */
         
         
         // ---- 핵심: 비동기 제출하여 dispatch 스레드는 빠르게 다음 항목으로 진행 ----
         // 결과 종속적이지 않은 독립된 것은 비동기
         outboxTaskExecutor.execute(() -> {
             boolean success = false;
             String errorMessage = null;
             Outbox current = outboxRepo.findById(outboxId).orElse(null);
             String payload = current != null ? current.getPayload() : null;
             try {
            	
            	 
				/*
				 * //실제는 어떻게 구현?? 
				 * //emailSender.send(payload); 
				 * 
				 */
            	 
            	 // emailSender.send은 성공시 정상 반환, 실패시 SmtpSendException 발생
                 success = true;
             } catch (SmtpSendException sse) {
                 success = false;
                 // SMTP가 4xx/5xx 응답을 반환한 경우(서버가 수신 거부 등)
                 errorMessage = "SMTP_ERROR:" + sse.getSmtpCode() + ":" + sse.getMessage();
             } catch (Exception ex) {
                 success = false;
                 // 네트워크/기타 문제
                 errorMessage = "SEND_ERROR:" + ex.getMessage();
             }

             // 결과 DB 반영
             try {
                 outboxProcessor.persistSendResult(outboxId, success, errorMessage);
             } catch (Exception ex) {
                 log.error("persistSendResult failed for outboxId={}", outboxId, ex);
                 // 운영정책: 실패시 DLQ/alert 고려
             }
         });//비동기
         
         
     }//loop
 }
}
