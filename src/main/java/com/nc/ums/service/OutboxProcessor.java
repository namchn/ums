package com.nc.ums.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nc.ums.domain.Message;
import com.nc.ums.domain.MessageStatus;
import com.nc.ums.domain.Outbox;
import com.nc.ums.domain.SendFailure;
import com.nc.ums.repo.MessageRepository;
import com.nc.ums.repo.OutboxRepository;
import com.nc.ums.repo.SendFailureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final MessageRepository msgRepo;
    private final OutboxRepository outboxRepo;
    private final SendFailureRepository failureRepo;
   
    // 하나의 메시지를 처리하는 단위 트랜잭션 메소드
    @Transactional
    public boolean claimMessage(Outbox r) {
    	
    	// 1. 선점/업데이트
    	Outbox out = outboxRepo.findById(r.getId()).orElse(null);
        if (out == null || Boolean.TRUE.equals(out.getPublished())) return false;
    	
        // 트렌젝선 내에서 처리해야 함
        //if (r == null || Boolean.TRUE.equals(r.getPublished())) return false;
        
        String messageId = out.getAggregateId();
        int updated = msgRepo.updateStatusIf(messageId, MessageStatus.NEW.name(), MessageStatus.PROCESSING.name());
        
        // 1 이면 처리 요청 ,0 은 이미 처리중임.
        if (updated == 0) {
            log.info("Message id={} already processed. Skipping.", messageId);
            return false; // 선점 실패, 트랜잭션 커밋 후 종료
        }

        log.info("Message id={} successfully claimed. Proceeding with dispatch.", messageId);
        
        // 2. (선택사항) 트랜잭션 커밋 후 외부 시스템 호출
        // 외부 호출은 트랜잭션과 독립적으로 처리하는 것이 좋습니다.
        // 이 부분은  Dispatcher 로직에서 처리하거나,
        // TransactionSynchronizationManager를 사용하여 커밋 성공 시 실행되도록 할 수 있습니다.
        
        //--> 트랜잭션은 여기서 즉시 커밋되고 끝납니다. DB 커넥션 반환
        return updated == 1;
    }
    
   
    
    @Transactional
    public void persistSendResult(Long outboxId, boolean success, String errorMessage) {
        Outbox out = outboxRepo.findById(outboxId).orElse(null);
        if (out == null) {
            log.error("persistSendResult: outbox not found id={}", outboxId);
            return;
        }

        String messageId = out.getAggregateId();

        Message msg = msgRepo.findByMessageId(messageId);
        if (msg == null) {
            log.error("persistSendResult: message not found messageId={}", messageId);
        } else {
            if (success) {
                // SMTP/외부 API가 2xx 응답을 줘서 정상적으로 메일서버에 전달된 상태
                //msg.setStatus(MessageStatus.DISPATCHED);
                msg.setStatus(MessageStatus.SUCCESS.name());
            } else {
                // errorMessage를 보고 분류
                if (errorMessage != null && errorMessage.startsWith("SMTP_ERROR:")) {
                    msg.setStatus(MessageStatus.DELIVERY_FAILED.name());
                } else {
                    msg.setStatus(MessageStatus.SEND_ERROR.name());
                }
                // 실패 카운트 증가
                Integer attempts = msg.getAttemptCount() == 0 ? 0 : msg.getAttemptCount();
                msg.setAttemptCount(attempts + 1);
            }
            msgRepo.save(msg);
        }

        // Outbox는 발송 시도 후에 published=true 로 마크
        out.setPublished(true);
        outboxRepo.save(out);

        if (!success) {
            SendFailure sf = SendFailure.builder()
                    .messageId(messageId)
                    .payload(out.getPayload())
                    .errorType(errorMessage != null ? errorMessage : "SEND_ERROR")
                    .errorMessage(errorMessage)
                    .handled(false)
                    .build();
            failureRepo.save(sf);
        }
    }

    
}
