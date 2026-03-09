# 스프링 부트 액추에이터 (Actuator)

스프링 부트 액추에이터는 애플리케이션의 상태를 모니터링하고 관리할 수 있는 엔드포인트를 제공한다. health, info, logger, httpexchanges 등 주요 엔드포인트의 설정과 활용 방법을 정리한다.

---

## 기본 설정

### 의존성 추가

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

### 엔드포인트 노출 설정

```yaml
management:
  server:
    port: 9292
  endpoints:
    web:
      exposure:
        include: "*"
```

- `management.server.port` : 액추에이터 엔드포인트를 별도 포트로 분리할 수 있다. 외부에는 애플리케이션 포트만 열고, 내부 네트워크에서만 액추에이터에 접근하도록 구성할 때 사용한다.
- `include: "*"` : 모든 엔드포인트를 웹에 노출한다. 운영 환경에서는 필요한 것만 선택적으로 노출해야 한다.

### 주요 엔드포인트 목록

| 엔드포인트 | 설명 |
|-----------|------|
| `beans` | 스프링 컨테이너에 등록된 스프링 빈을 보여준다 |
| `conditions` | condition을 통해 빈을 등록할 때 평가 조건과 일치 여부를 표시한다 |
| `configprops` | `@ConfigurationProperties`를 보여준다 |
| `env` | Environment 정보를 보여준다 |
| `health` | 애플리케이션 헬스 정보를 보여준다 |
| `httpexchanges` | HTTP 호출 응답 정보를 보여준다 |
| `info` | 애플리케이션 정보를 보여준다 |
| `loggers` | 애플리케이션 로거 설정을 보여주고 변경도 할 수 있다 |
| `metrics` | 애플리케이션의 메트릭 정보를 보여준다 |
| `mappings` | `@RequestMapping` 정보를 보여준다 |
| `threaddump` | 쓰레드 덤프를 실행해서 보여준다 |
| `shutdown` | 애플리케이션을 종료한다 (기본 비활성화) |

---

## 1. Health

애플리케이션이 정상적으로 동작하고 있는지 확인하는 엔드포인트이다. DB, 디스크 공간, Redis 등 연동된 시스템의 상태까지 함께 확인한다.

### 설정

```yaml
management:
  endpoint:
    health:
      show-components: always
```

| 옵션 | 설명 |
|------|------|
| `show-components: always` | 헬스 정보의 각 컴포넌트(db, diskSpace 등)를 항상 표시한다 |
| `show-details: always` | 컴포넌트보다 더 상세한 정보(DB 종류, 디스크 용량 등)를 표시한다 |

### 응답 예시

```
GET http://localhost:9292/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

- `status: UP` : 모든 컴포넌트가 정상이면 `UP`, 하나라도 문제가 있으면 `DOWN`이 된다.
- 스프링 부트는 의존성에 따라 자동으로 헬스 체크 항목을 추가한다. (예: `spring-boot-starter-data-jpa` → db 헬스 체크 자동 추가)

---

## 2. Info

애플리케이션의 기본 정보를 제공하는 엔드포인트이다.

### 설정

```yaml
management:
  info:
    java:
      enabled: true
    os:
      enabled: true
    env:
      enabled: true

info:
  app:
    name: hello-actuator
    company: bang
    version: 0.0.1
```

| 정보 소스 | 설정 | 설명 |
|----------|------|------|
| `env` | `management.info.env.enabled: true` | `info.*` 로 시작하는 프로퍼티를 노출한다 |
| `java` | `management.info.java.enabled: true` | 자바 런타임 정보를 노출한다 |
| `os` | `management.info.os.enabled: true` | OS 정보를 노출한다 |
| `build` | `springBoot { buildInfo() }` (build.gradle) | 빌드 정보를 노출한다 |
| `git` | `com.gorylenko.gradle-git-properties` 플러그인 | Git 커밋 정보를 노출한다 |

### build.gradle 추가 설정

```gradle
plugins {
    id "com.gorylenko.gradle-git-properties" version "2.4.1"  // git info
}

springBoot { buildInfo() }  // build info
```

- `buildInfo()` : 빌드 시 `META-INF/build-info.properties` 파일을 생성해서 빌드 시간, 버전 등을 제공한다.
- `gradle-git-properties` : 빌드 시 `git.properties` 파일을 생성해서 Git 커밋 해시, 브랜치 등을 제공한다.

### 응답 예시

```
GET http://localhost:9292/actuator/info
```

```json
{
  "app": {
    "name": "hello-actuator",
    "company": "bang",
    "version": "0.0.1"
  },
  "java": {
    "version": "17.0.x",
    "vendor": { ... }
  },
  "os": {
    "name": "Mac OS X",
    "version": "...",
    "arch": "aarch64"
  },
  "build": {
    "artifact": "actuator",
    "version": "0.0.1-SNAPSHOT",
    "time": "..."
  },
  "git": {
    "branch": "main",
    "commit": { "id": "...", "time": "..." }
  }
}
```

---

## 3. Loggers

애플리케이션의 로거 설정을 조회하고, **실행 중에 로그 레벨을 변경**할 수 있는 엔드포인트이다. 애플리케이션 재시작 없이 로그 레벨을 변경할 수 있어서 운영 환경에서 유용하다.

### 로그 레벨 설정

```yaml
logging:
  level:
    hello.controller: debug
```

### 로그를 출력하는 컨트롤러

```java
@Slf4j
@RestController
public class LogController {

    @GetMapping("/log")
    public String logger() {
        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        return "ok";
    }
}
```

- `hello.controller` 패키지의 로그 레벨이 `debug`이므로 `trace`는 출력되지 않고 `debug` 이상만 출력된다.

### 로그 레벨 조회

```
GET http://localhost:9292/actuator/loggers/hello.controller
```

```json
{
  "configuredLevel": "DEBUG",
  "effectiveLevel": "DEBUG"
}
```

| 필드 | 설명 |
|------|------|
| `configuredLevel` | 명시적으로 설정한 로그 레벨 |
| `effectiveLevel` | 실제 적용되고 있는 로그 레벨 (상위 로거에서 상속될 수 있음) |

### 실행 중 로그 레벨 변경

```
POST http://localhost:9292/actuator/loggers/hello.controller
Content-Type: application/json

{
  "configuredLevel": "TRACE"
}
```

- POST 요청으로 로그 레벨을 **실시간으로 변경**할 수 있다.
- 위 요청 후 `/log`를 호출하면 `trace`까지 출력된다.
- 애플리케이션을 재시작하면 설정 파일의 원래 값으로 돌아간다.

> 운영 중 특정 문제를 디버깅할 때 일시적으로 로그 레벨을 낮춰서 상세 로그를 확인하고, 이후 다시 원래 레벨로 올리는 방식으로 활용한다.

---

## 4. HTTP Exchanges

최근 HTTP 요청/응답 정보를 확인할 수 있는 엔드포인트이다. 이 엔드포인트를 사용하려면 `HttpExchangeRepository`를 구현한 빈을 별도로 등록해야 한다.

### 빈 등록

```java
@SpringBootApplication
public class ActuatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActuatorApplication.class, args);
    }

    @Bean
    public InMemoryHttpExchangeRepository httpExchangeRepository() {
        return new InMemoryHttpExchangeRepository();
    }
}
```

- `InMemoryHttpExchangeRepository` : 스프링 부트가 제공하는 기본 구현체로, 최대 100개의 HTTP 요청을 메모리에 저장한다.
- 최대 요청 수를 넘으면 과거 요청을 삭제한다. `setCapacity()`로 변경할 수 있다.

### 응답 예시

```
GET http://localhost:9292/actuator/httpexchanges
```

```json
{
  "exchanges": [
    {
      "timestamp": "...",
      "request": {
        "uri": "http://localhost:8080/log",
        "method": "GET",
        "headers": { ... }
      },
      "response": {
        "status": 200,
        "headers": { ... }
      },
      "timeTaken": "PT0.005S"
    }
  ]
}
```

- 요청 URI, HTTP 메서드, 응답 상태코드, 소요 시간 등을 확인할 수 있다.
- 메모리에 저장하므로 애플리케이션 재시작 시 데이터가 사라진다.

### 한계

| | InMemoryHttpExchangeRepository | 실무 |
|---|-------------------------------|------|
| **저장소** | 메모리 (휘발성) | 별도 저장소 (DB, 파일 등) |
| **용량** | 최대 100개 | 제한 없음 |
| **기능** | 단순 조회 | 필터링, 검색, 대시보드 |

> 개발 단계에서 간단히 확인하는 용도로 적합하다. 운영 환경에서는 핀포인트(Pinpoint), Zipkin 등 전문 모니터링 도구를 사용한다.

---

## 전체 흐름 정리

```
[액추에이터 설정]

의존성 추가 (spring-boot-starter-actuator)
  │
  ▼
엔드포인트 노출 설정 (management.endpoints.web.exposure.include)
  │  운영 환경에서는 필요한 것만 선택적으로 노출
  │  액추에이터 포트를 분리하여 내부 네트워크에서만 접근 가능하게 구성
  ▼

[주요 엔드포인트]

health → 애플리케이션과 연동 시스템의 상태 확인
  │  DB, 디스크, Redis 등 자동 감지
info → 애플리케이션 정보 (env, java, os, build, git)
  │  build.gradle에 buildInfo(), git-properties 플러그인 추가
loggers → 로그 레벨 조회 및 실시간 변경
  │  POST 요청으로 재시작 없이 로그 레벨 변경 가능
httpexchanges → 최근 HTTP 요청/응답 이력
     InMemoryHttpExchangeRepository 빈 등록 필요
     개발용, 운영에서는 전문 모니터링 도구 사용
```
