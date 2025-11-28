
```
#간략 흐름도

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



# 추가 개발구현 목표
```
 1. 확장성 고려
 2. 정교화
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


