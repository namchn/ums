package com.nc.ums.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nc.ums.domain.Message;
import com.nc.ums.domain.Outbox;
import com.nc.ums.repo.MessageRepository;
import com.nc.ums.repo.OutboxRepository;
import com.nc.ums.service.exception.ConstraintHandlerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository msgRepo;
    private final OutboxRepository outboxRepo;
    private final ConstraintHandlerService constraintHandlerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public boolean enqueueMessage(String messageId, Map<String,Object> payload) {

        String payloadStr;
        try {
            payloadStr = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("payload serialize failed", e);
        }

        try {
            // 1) 메시지 저장 (중복이면 예외)
            Message m = Message.builder()
                    .messageId(messageId)
                    .payload(payloadStr)
                    .status("NEW")
                    .build();
            msgRepo.save(m);

            // 2) outbox 저장 (같은 트랜잭션)
            Outbox out = Outbox.builder()
                    .aggregateId(messageId)
                    .topic("message-topic")
                    .payload(payloadStr)
                    .published(false)
                    .build();
            outboxRepo.save(out);
            //Outbox는 DB와 브로커 간 정합성을 보장하는 패턴. “트랜잭션 내에서 기록”이 핵심

            return true;

        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // ❗중요: 여기서는 "현재 트랜잭션"이 이미 rollback-only 상태임.
            // → 어떤 DB 작업도 하면 안 된다.
            // → 대신: 새 트랜잭션 메서드를 호출한다.

            return constraintHandlerService.handleConstraintInNewTx(messageId);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    /*
    
    //예외 발생 처리 — 새 트랜잭션(REQUIRES_NEW)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean handleConstraintInNewTx(String messageId) {

        // DB 접근 가능 (별도 트랜잭션이므로 안전)
        Message existing = msgRepo.findByMessageId(messageId);

        if (existing == null) {
            // 이 경우는 진짜 장애 → false가 아닌 예외를 던져서 운영에서 알도록 해야 함
            throw new RuntimeException("Message insert failed but no existing record found");
        }

        if ("SUCCESS".equals(existing.getStatus())) {
            // 이미 성공 처리된 메시지 → 안전하게 false
            return false;
        }

        // 재처리가 필요한 상태 (FAILED / PROCESSING 등)
        // outbox를 새로 만들어주는 것이 정책적으로 더 안전
        Outbox out = Outbox.builder()
                .aggregateId(messageId)
                .topic("message-topic")
                .payload(existing.getPayload())
                .published(false)
                .build();
        outboxRepo.save(out);

        return true; // 재처리 허용
    }
    
    */
}
