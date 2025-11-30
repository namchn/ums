

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
│ │ └── AsyncConfig : 비동기 처리 쓰레드 관련 값 설정
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



#개선점 및 구현 목표 
```
 1. 확장성 고려
 2. 정교화
 3. 실패시 (false -> log + alert) 로직
```




#소스 팁
```
 1. 클래스에 @EnableScheduling 붙이는 건 보통 @Configuration에 두는 게 좋음(전역 설정)
 2. 상태 문자열 상수화 (enum or constants class)
 3. 비동기는 트렌젝션 경계  밖에서 실행
 4. claimMessage 구현(영속성 컨텍스트 주의) -> 이미 로드하여 가져온 파라미터 Message 인스턴스를 재사용 하지 않기 
```









#남은 작업(우선순위별 체크리스트)
```
우선순위 목록 (실전 권장 순서)

P1 — 운영성·가시성 (우선 적용 권장)

Micrometer 메트릭 추가

목표: outbox.backlog, send.failures{type}, claim.failures, dispatch.duration 등 수집

검증: Prometheus(또는 local meter registry)에서 메트릭 확인

Structured logging 규칙 통일

목표: 모든 핵심 로그에 messageId, outboxId, attemptCount, errorType 포함

검증: 로그 검색에서 필드로 필터링 가능

EmailSender 인터페이스화 + 테스트 더블

목표: EmailSender를 인터페이스로 추출, 테스트용 NoopEmailSender/FailingEmailSender 구현

검증: Unit test에서 실제 네트워크 호출 없이 시나리오 검증

OutboxRepository 보조 메서드 추가/인덱스

목표: existsByAggregateIdAndPublishedFalse, findTopNByPublishedFalseOrderByCreatedAt 추가 + DB 인덱스(aggregateId, published, createdAt)

검증: 쿼리 실행계획 및 성능 확인

AdminController 단건 재시도 경로 완성

목표: PATCH /admin/failures/{id}/retry 구현 (권한 보호, audit 기록)

검증: 관리자 요청 시 Outbox 재생성 및 handled=true 기록

P2 — 안정성·정책

DLQ / Manual review flow

목표: attemptCount >= MAX 시 SendFailure → DLQ 이동 또는 MANUAL_REVIEW 상태

검증: 시뮬레이션으로 attempts 초과 시 DLQ row 생성

PROCESSING 스테일 탐지(간단 버전)

목표: processingStartedAt 필드 추가(선점시 set), 스케줄러로 오래된 PROCESSING을 재큐

검증: 인위적 타임스탬프 변경 후 reclaim 동작 확인

Payload validation + rate limiting

목표: DTO 기반 validation + request size limit + simple rate limiting (IP or API key)

검증: Invalid payload -> 400, Rate limit exceed -> 429

P3 — 중장기·확장

Unit & Integration 테스트 완성

목표: Testcontainers 기반 E2E 테스트(duplicate inserts, claim race, success/failure flows)

검증: CI에서 테스트 통과

Service 계층 인터페이스화 (MessageService, OutboxService)

목표: 인터페이스 추출, 구현 교체 용이성 확보

검증: 테스트에서 mocking으로 서비스 교체 가능

이벤트 기반 아키텍처 고려 (Kafka/CDC)

목표: 대량 처리·리플레이 요구 발생 시 Outbox→Kafka 전환 설계문서 작성

검증: PoC로 DB outbox→Kafka producer 구현(소규모)

Loan 도메인 상태머신 도입

목표: Loan 상태 전이는 state machine으로 관리(규칙/테스트 쉬움)

검증: 상태별 전이 테이블과 유닛 테스트
```



#깃 기본 명령어 모음
```

git init
git remote add origin https://github.com/namchn/ums.git
git pull origin main --rebase
git add .
git commit -m "1st commit"
git push origin main

```

```
부분별로 chatGPT 를 활용하여 학습 및 개발, 작성 하였습니다.
```
