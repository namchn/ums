

#간략 흐름도
```

┌────────────────────────────┐
│ 1. REST API 발송 요청       │
│ POST /api/messages         │
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│ Message 저장 (원본 데이터)   │
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│ Outbox 저장 (발송 큐)       │
└─────────────┬──────────────┘
              │
             (비동기)
              │
              ▼
┌────────────────────────────┐
│ OutboxDispatcher           │
│ (5초마다 미발행 Outbox 조회) │
├────────────────────────────┤
│ SMTP/메일 API 호출(시뮬)     │
│ published = true 변경       │
└────────────────────────────┘

```




#프로젝트 구조
```
ums/
├── domain/
│ ├── Message : 메시지 원장 엔티티
│ ├── MessageStatus : 메시지 상태 enum
│ └── Outbox : 메시지 발송 엔티티
├── email/
│ ├── config/
│ │ └── AsyncConfig : 동기 처리 쓰레드 관련 값 설정
│ ├── EmailSender : 이메일 발송 로직
│ ├── SmtpSendException : 이메일 발송 예외처리값
├── service/
│ ├── exception/
│ │ └── ConstraintHandlerService : MessageService 예외 처리 로직
│ ├── MessageService : 이메일 발송 로직
│ ├── OutboxDispatcher: 이메일 발송 스케쥴러 -> 배치 변경 필요 
│ └── OutboxProcessor : 트렌젝션 단위 메소드 로직
├── web/
│ └──MessageController :  메일 발송 요청 컨트롤러
└── ResumeApplication : 스프링부트 메인

```



# 개선점 및 구현 목표 
```
 1. 확장성 고려
 2. 정교화
 3. 실패시 (false -> log + alert) 로직
```




# 소스 팁
```
 1. 클래스에 @EnableScheduling 붙이는 건 보통 @Configuration에 두는 게 좋음(전역 설정)
 2. 상태 문자열 상수화 (enum or constants class)
 3. 비동기는 트렌젝션 경계  밖에서 실행
 4. claimMessage 구현(영속성 컨텍스트 주의) -> 이미 로드하여 가져온 파라미터 Message 인스턴스를 재사용 하지 않기 
```









#남은 작업(우선순위별 체크리스트)
```
아래는 실무 운영·면접 준비 관점에서 우선순위를 매긴 작업 목록입니다. (위에서 가장 중요한 것부터)

P0 — 반드시 처리해야 할 것 (안정성/정합성)

attemptCount null-safe 고치기 (이미 리마인드했지만 최우선).

persistSendResult에서 상태값 일관성 유지(성공 → DISPATCHED or SUCCESS 중 팀 표준으로 통일).

ConstraintHandlerService 보수적 기본 동작으로 고정 (unknown → false + log + alert).

Outbox published=true 타이밍 재확인 (반드시 전송 시도 후).

updateStatusIf 사용 패턴 재검증(선점은 update만, 이후에는 새 트랜잭션에서 조회/저장).

P1 — 운영성·관찰성(모니터링 포함)

ThreadPool/ Scheduling 설정을 config로 외부화 (application.yml) 및 모니터링 지표 추가.

Micrometer 도입: outbox.backlog, send.failure.count{type}, claim.failures, dispatch.timer.

Structured logs: 항상 messageId, outboxId, attemptCount 포함.

Test doubles: EmailSender 테스트 구현(성공/SMTP-fail/IO-fail).

P2 — 기능 보강(운영 편의성)

AdminController + FailureAdminService (단건 재시도, handled 플래그) 구현 완료.

OutboxRepository 보조 메서드: existsByAggregateIdAndPublishedFalse, findTopNUnpublishedOrderByCreatedAt.

Add DLQ / manual review path when attempts exceed threshold.

Add processingStartedAt in Message entity (future stale detection).

P3 — 중장기(스케일 / 아키텍처)

Unit + Integration test suite (Testcontainers for DB).

Service 계층 인터페이스화 (MessageService, OutboxService) → mockability.

Consider event-driven: Outbox → Kafka producer or Debezium CDC for high scale.

Status machine for Loan domain (Spring State Machine or custom).
```



# 깃 기본 명령어 모음
```

git init
git remote add origin https://github.com/namchn/ums.git
git pull origin main --rebase
git add .
git commit -m "1st commit"
git push origin main

```


