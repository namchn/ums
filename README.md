

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









#설계에서 고려할 점.
```

아키텍처 / 설계

1. Outbox 패턴
DB 트랜잭션과 외부 전송 간 정합성을 보장하기 위해, 
Message(원장)+Outbox(발행)를 동일 트랜잭션에 저장하고 외부 전송은 Outbox를 소비하는 방식으로 분리.

2.멱등성 보장
클라이언트 제공 ID와 DB unique 제약으로 식별하고, 
중복 충돌은 별도 REQUIRES_NEW 트랜잭션에서 판단.

3.동시성 제어(선점)
DB 조건부 업데이트로 선점하고 반환된 row count로 성공/실패(0/1)를 판단하는 
lock-free CAS 패턴 (UPDATE ... WHERE status='NEW')사용.

4.전송 실패 관리
전송은 트랜잭션 경계 밖에서 수행하고, 결과만 별도 트랜잭션에서 반영하여 
실패는 SendFailure에 적재하고 재시도/수동 재처리(DLQ/manual) 정책으로 관리.


트랜잭션 / JPA

5.JPQL update 후 영속성 컨텍스트
JPQL update는 DB만 변경하므로 동일 트랜잭션에서 이미 로드한 엔티티를 신뢰하지 않고, 
필요한 경우 재조회하거나 트랜잭션을 분리.

6.메일 발송 호출(send()) 트랜잭션 밖에서 호출
네트워크 호출은 지연 요인이므로 DB 트랜잭션 밖에서 실행해 커넥션/락을 비동기화


운영 · 확장성

7.모니터링 방법
outbox 에 실패 데이터(backlog, failure rate) 알람, 이상 시 DLQ/수동 개입 루트를 활성화.

8.대량 발송 시 병목 해소 방법
worker pool, rate limiting, TPS 제어, batching, Outbox→Kafka로 전환.


보안·규정

9. 개인 정보 관리 
민감정보는 DB에 암호화 저장하고 admin 작업은 접근 제어.


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
