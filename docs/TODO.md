# 📝 운영/고도화 대비 TODO List

## 1. 알림 시스템 연동 (Webhook)
* **목표**: 시스템 내 500 에러 또는 서킷 브레이커 차단(Open) 발생 시 개발팀으로 즉각적인 알림 발송.
* **작업 사항**:
  * Logback에 커스텀 Appender(`SlackAppender` 등)를 생성하거나 외부 라이브러리 연동.
  * 글로벌 예외 처리기(`GlobalExceptionHandler`)와 `Resilience4j` 상태 변화 이벤트(`onStateTransition`) 리스너에서 트리거 호출.

## 2. API 인증/인가(Authorization) 고도화
* **목표**: 현재 열려있는 모든 API(`/api/**`)를 가맹점 및 사용자 인증을 통해 보호.
* **작업 사항**:
  * `SecurityConfig`에 JWT 검증 필터 또는 API Key(Merchant Key) 검증 필터 구현 및 등록.
  * 권한이 없는 접근 시 401(Unauthorized) 또는 403(Forbidden) 응답 처리.