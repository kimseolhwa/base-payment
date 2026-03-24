# 🚀 Base Payment (결제 시스템 기본 프로젝트)
---
본 프로젝트는 다양한 결제 환경에서 유연하고 안정적으로 동작할 수 있도록 설계된 **결제 시스템 구축을 위한 탄탄한 뼈대(Base) 프로젝트**입니다.
글로벌 결제 표준 파라미터(`merchantId`, `traceId`)를 채택하고, 대규모 트래픽 환경에서 필수적인 장애 격리(Circuit Breaker) 및 중복 결제 방지(Idempotency)를 코어 프레임워크 레벨에서 완벽하게 지원합니다.


## 🛠 기술 스택 (Tech Stack)

### Backend
- **Core:** Java 21, Spring Boot 3.2.x (내장 Tomcat)
- **Database / ORM:** MySQL, MyBatis, HikariCP
- **Cache & Session:** Ehcache 3 (로컬 L2 캐시), Redis (분산 락 및 글로벌 세션)
- **Resilience:** Resilience4j (서킷 브레이커)
- **Template Engine:** Thymeleaf (결제창 UI 폼 렌더링 전용)

### Frontend (External 연동)
- **Core:** Next.js (App Router), TypeScript, CSS Modules

---

## 📂 아키텍처 및 디렉토리 구조 (Architecture)

도메인 주도 설계(DDD)의 철학과 실무에서 가장 직관적인 **계층형 아키텍처(Layered Architecture)**를 결합하여 구성했습니다.

### 1. Java 패키지 구조 (`com.github.shkim.base`)
```text
base/
 ├── common/               # [Core] 시스템 전역 공통 인프라 (Filter, Interceptor, AOP, Exception)
 ├── inquiry/              # Phase 1: DB 조회 API 도메인 (Java 21 record 적용)
 ├── noti/                 # Phase 2: 노티 발송 API 도메인 (Resilience4j 적용)
 └── cert/                 # Phase 3: 결제 인증 UI 연동 도메인 (Redis 세션 적용)
```
*각 도메인 하위는 `controller`, `service`, `mapper`, `dto` 계층으로 엄격히 분리하여 관리합니다.*

### 2. 리소스 구조 (`src/main/resources`)
```text
resources/
 ├── application.yml       # 전역 설정 (Graceful Shutdown, L4 Health Check, 서킷 브레이커 등)
 ├── logback-spring.xml    # 일별 롤링 및 용도별(info, error, sql) 파일 라우팅, MDC 패턴 적용
 ├── db/                   # 환경별(Profile) DB 접속 정보 격리 (local, dev, real)
 │    └── h2/              # 로컬 테스트용 H2 DB 자동 초기화 스크립트 (schema, data)
 ├── certs/                # 외부 기관 연동용 물리 인증서(.p12, .pem) 격리
 └── mapper/               # 주제 중심 매퍼 분리 (site, auth, txn, common)
```

---

## ✨ 핵심 인프라 및 방어 체계 (Core Features)

### 🛡 1. 대용량 트래픽 장애 대비 (Resilience & Idempotency)
- **서킷 브레이커 (`externalApi`):** `Resilience4j`를 적용하여 외부 기관(노티 발송 등) 통신 지연 및 에러 임계치 초과 시 즉각 통신을 차단(Fail-Fast)하고 Fallback 로직을 실행하여 연쇄 장애 방지.
- **API 멱등성 보장 (`@Idempotent`):** 결제 등 중요한 쓰기 API 호출 시 `Idempotency-Key` 헤더를 가로채어 Redis 분산 락(Lock) 획득. 동시 다발적인 따닥 요청을 차단하고, 이전 성공 응답값을 자동 캐싱하여 즉시 반환.

### 📊 2. 추적 불패의 로깅 시스템 (Tracing)
- **글로벌 Trace ID (`traceId`):** `CachedBodyHttpServletRequest`를 통해 HTTP Body를 사전 캐싱하여 `merchantId`를 추출, 전 구간을 관통하는 고유 식별자(`traceId`) 발급 및 MDC 컨텍스트 유지.
- **정밀 데이터 마스킹:** 로그 출력 전 정규식 및 JSON 재귀 탐색을 통해 카드번호 등 민감 개인정보 완벽 마스킹 처리 (`MaskingUtil`).
- **성능 측정 (AOP):** `StopWatch`를 활용하여 API 응답, DB 쿼리 실행, 외부 연동 소요 시간을 로깅 (`PerformanceLoggingAspect`).

### 🚦 3. 강력한 보안 및 무중단 운영 (Security & Stability)
- **보안 및 암호화:** XSS 방어(Jsoup 래퍼, Jackson 이스케이프), 보안 헤더 5종 강제 주입. 기동 시점 KMS 메모리 캐싱을 통한 빠른 대칭키 암복호화.
- **안정성:** L4 로드밸런서 헬스체크 지원(`/health`), Graceful Shutdown 적용, API(JSON)/UI(HTML) 에러 응답 자동 라우팅.

---

## 🚀 로컬 실행 방법 (Getting Started)

본 프로젝트는 리뷰어의 빠른 테스트를 위해 별도의 인프라 설치 없이 즉시 기동되는 **Zero-Setup 로컬 환경**을 제공합니다.

1. **In-memory DB & Redis 자동 기동:** `local` 프로파일 기동 시, **H2 Database**와 **Embedded Redis**가 애플리케이션 라이프사이클에 맞춰 메모리상에 자동 기동 및 종료됩니다.
2. **더미 데이터 자동 삽입:** 기동 시점에 `schema.sql` 및 `data.sql`이 동작하여 테스트용 결제 내역(Phase 1 조회용)이 즉시 적재됩니다.
3. **서킷 브레이커 테스트:** 노티 발송 API(Phase 2) 호출 시, 의도된 더미 포트(9999)로 통신을 시도하여 Resilience4j의 차단(Open) 및 Fallback 로직을 즉시 눈으로 확인할 수 있습니다.

```bash
# 별도의 인프라 구성 없이 바로 서버 기동 가능
$ ./gradlew bootRun --args='--spring.profiles.active=local'
```

---
## 🧪 테스트 및 품질 보증 전략 (Testing Strategy)

본 프로젝트는 코드의 신뢰성을 보장하고 기술 부채를 방지하기 위해 엄격한 테스트 표준을 강제합니다.

### 1. 테스트 프레임워크 표준
- **Core:** JUnit 5 (Jupiter), AssertJ
- **Mocking:** Mockito (`@ExtendWith(MockitoExtension.class)`)
- **패턴:** `given-when-then` (BDD 스타일) 구조를 반드시 준수합니다.

### 2. 계층별 단위 테스트(Unit Test) 정책
- **Service Layer (비즈니스 로직):**
    - 스프링 컨텍스트(`@SpringBootTest`) 로드를 지양하고, Mockito를 활용한 **격리된 단위 테스트**를 원칙으로 합니다.
    - 외부 통신(Client) 및 DB 연결(Mapper)은 반드시 Mock 객체로 대체하여, 서버 환경에 독립적이고 빠른 테스트 실행 속도를 보장합니다.
- **Controller Layer (API 인터페이스):**
    - 필요시 `@WebMvcTest`를 활용하여 파라미터 바인딩 및 `@Valid` 어노테이션 기반의 예외 처리 로직을 검증합니다.

### 3. CI/CD 연동 (예정)
- GitHub Actions 배포 파이프라인에서 `./gradlew test` 수행 시 실패하는 테스트가 단 1개라도 존재하면 즉시 빌드를 중단(Fail-Fast)하여 결함이 있는 코드의 운영 환경 배포를 원천 차단합니다.

---

## 📝 코딩 컨벤션 (Conventions)

본 프로젝트는 아래의 엄격한 엔터프라이즈 코딩 표준을 따릅니다.

1. **Modern Java 및 DTO 전략**
    - 무분별한 DTO 클래스 생성을 방지하기 위해 Java 21의 `record`를 활용하여 불변 객체 및 파라미터 검증(`@Valid`) 구조를 간결화.
    - 중첩 `if`문 지양, `Optional` 체이닝 및 `Stream API` 적극 활용.

2. **국문 Javadoc 표준 주석**
    - 불필요한 서술형 어미를 배제하고 **`~수행`, `~반환`, `~처리`** 등 간결한 개조식 명사형 종결 사용.
    - 클래스 및 핵심 비즈니스 메서드에 `@param`, `@return` 명시 필수.

---
