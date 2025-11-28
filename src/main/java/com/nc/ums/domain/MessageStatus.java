package com.nc.ums.domain;


public final class MessageStatus {
    private MessageStatus() {}

    public static final String NEW = "NEW";
    public static final String PROCESSING = "PROCESSING";
    // 우리가 새로 명확히 쓰는 상태명
    public static final String DISPATCHED = "DISPATCHED";        // SMTP(또는 외부 API)로 정상 전달(2xx 응답)
    public static final String DELIVERY_FAILED = "DELIVERY_FAILED"; // SMTP가 오류 응답(4xx/5xx)
    public static final String SEND_ERROR = "SEND_ERROR";        // 네트워크/타임아웃/예외 등 (서버 자체 에러)
    public static final String SUCCESS = "SUCCESS"; // 기존 호환용(필요시 유지)
    public static final String FAILED = "FAILED";   // 기존 호환용(필요시 유지)
    public static final String RETRY = "RETRY";
}