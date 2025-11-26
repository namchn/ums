package com.nc.ums.web;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nc.ums.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService msgService;

	/*
	 * STEP1(멱등): messageId + DB unique로 멱등 보장. 동시성은 unique 제약 + 예외흐름으로 처리.
	 * STEP2(Outbox): 트랜잭션 경계 문제 해결 — DB저장과 외부발행 간 정합성.
	 * STEP3(Failure): 자동 재시도 위험성 → 실패 적재 → 운영자 검증 후 재처리 원칙.
	 */
    
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestHeader(value = "X-Message-Id", required = false) String messageId,
                                         @RequestBody Map<String,Object> payload) {
        if (messageId == null || messageId.isBlank()) messageId = UUID.randomUUID().toString();
        
        
        //boolean accepted = msgService.enqueueMessage(messageId, payload);
        
        boolean accepted;
        try {
            accepted = msgService.enqueueMessage(messageId, payload);
        } catch (org.springframework.transaction.UnexpectedRollbackException ex) {
            // 하위 서비스에서 이미 처리된 예외이므로 성공으로 간주
            accepted = false; // 또는 true, 비즈니스 로직에 맞게 설정
            // accepted = constraintHandlerService.handleConstraintInNewTx(messageId); // 이 방법도 가능
        }
        
        if (!accepted) {
            return ResponseEntity.ok(Map.of("status","duplicate","messageId",messageId));
        }
        
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status","accepted","messageId",messageId));
    }
}
