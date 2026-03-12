# 마이크로미터와 메트릭

모니터링 툴(그라파나, 핀포인트 등)에 시스템 정보를 전달하려면 각 툴이 정한 포맷에 맞춰야 한다. 모니터링 툴을 변경하면 기존 측정 코드까지 모두 변경해야 하는 문제가 발생하는데, 마이크로미터가 이 문제를 추상화를 통해 해결한다. SLF4J가 로그 라이브러리를 추상화하는 것처럼, 마이크로미터는 메트릭 수집을 추상화해서 구현체를 쉽게 갈아 끼울 수 있도록 해준다.

---

## 기본 설정

### 의존성 추가

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'  // 프로메테우스 구현체
}
```

- `micrometer-registry-prometheus` : 마이크로미터가 수집한 메트릭을 프로메테우스 포맷으로 변환해준다.
- 프로메테우스 외에도 Datadog, New Relic 등 다양한 모니터링 툴의 구현체를 선택할 수 있다.

### 엔드포인트 설정

```yaml
management:
  server:
    port: 9292
  endpoints:
    web:
      exposure:
        include: "*"

server:
  tomcat:
    mbeanregistry:
      enabled: true  # 톰캣 메트릭 활성화
```

---

## 메트릭 조회 방법

`/actuator/metrics` 엔드포인트를 통해 기본 제공 메트릭을 확인할 수 있다.

### 기본 조회

```
GET /actuator/metrics                           → 사용 가능한 메트릭 목록
GET /actuator/metrics/{name}                    → 특정 메트릭 상세 조회
```

### Tag 기반 필터링

`tag=KEY:VALUE` 형식으로 메트릭을 필터링할 수 있다.

```
GET /actuator/metrics/jvm.memory.used                      → JVM 메모리 사용량
GET /actuator/metrics/jvm.memory.used?tag=area:heap        → 힙 메모리만 필터
GET /actuator/metrics/http.server.requests                 → HTTP 요청 수
GET /actuator/metrics/http.server.requests?tag=uri:/log    → /log 요청만 필터
```

---

## 다양한 메트릭

마이크로미터와 액추에이터가 기본으로 제공하는 메트릭을 정리한다.

### 1. JVM 메트릭

`jvm.` 으로 시작한다.

| 메트릭 | 설명 |
|--------|------|
| `jvm.memory.used` | 메모리 사용량 |
| `jvm.memory.max` | 최대 메모리 |
| `jvm.gc.pause` | 가비지 수집 관련 통계 |
| `jvm.threads.live` | 활성 스레드 수 |
| `jvm.classes.loaded` | 로드된 클래스 수 |

### 2. 시스템 메트릭

`system.`, `process.`, `disk.` 으로 시작한다.

| 메트릭 | 설명 |
|--------|------|
| `system.cpu.usage` | 시스템 CPU 사용률 |
| `process.cpu.usage` | 프로세스 CPU 사용률 |
| `process.uptime` | 가동 시간 |
| `disk.free` | 사용 가능한 디스크 공간 |

### 3. 스프링 MVC 메트릭

메트릭 이름: `http.server.requests`

스프링 MVC 컨트롤러가 처리하는 모든 요청을 다룬다. TAG를 사용해서 다음 정보를 분류할 수 있다.

| TAG | 설명 | 예시 |
|-----|------|------|
| `uri` | 요청 URI | `/log`, `/cpu` |
| `method` | HTTP 메서드 | `GET`, `POST` |
| `status` | HTTP 상태 코드 | `200`, `400`, `500` |
| `exception` | 예외 클래스 | - |
| `outcome` | 상태코드 그룹 | `SUCCESS`, `CLIENT_ERROR`, `SERVER_ERROR` |

### 4. 데이터소스 메트릭

`jdbc.connections.` 으로 시작한다.

| 메트릭 | 설명 |
|--------|------|
| `jdbc.connections.active` | 활성 커넥션 수 |
| `jdbc.connections.max` | 최대 커넥션 수 |
| `jdbc.connections.min` | 최소 커넥션 수 |

- 히카리 커넥션 풀을 사용하면 `hikaricp.` 를 통해 더 자세한 메트릭을 확인할 수 있다.

### 5. 로그 메트릭

`logback.events` : logback 로그에 대한 메트릭이다.

- `trace`, `debug`, `info`, `warn`, `error` 각각의 로그 레벨에 따른 로그 수를 확인할 수 있다.
- `error` 로그 수가 급격히 높아진다면 위험한 신호로 받아들일 수 있다.

### 6. 톰캣 메트릭

`tomcat.` 으로 시작한다.

```yaml
server:
  tomcat:
    mbeanregistry:
      enabled: true
```

- 위 옵션을 켜야 톰캣 메트릭을 모두 사용할 수 있다. 켜지 않으면 `tomcat.session.` 관련 정보만 노출된다.
- 최대 쓰레드, 사용 쓰레드 수를 포함한 다양한 메트릭을 확인할 수 있다.

---

## 장애 테스트용 컨트롤러

메트릭이 실제로 어떻게 변하는지 확인하기 위한 테스트용 엔드포인트이다.

### CPU 부하

```java
@GetMapping("cpu")
public String cpu() {
    long value = 0;
    for (int i = 0; i < 100000000000L; i++) {
        value++;
    }
    return "ok value =" + value;
}
```

- 호출하면 `system.cpu.usage`, `process.cpu.usage` 메트릭이 급격히 올라가는 것을 확인할 수 있다.

### JVM 메모리 부하

```java
private List<String> list = new ArrayList<>();

@GetMapping("jvm")
public String jvm() {
    for (int i = 0; i < 100000000000L; i++) {
        list.add("hello jvm + " + i);
    }
    return "ok value =" + list.size();
}
```

- 리스트에 계속 데이터를 추가하여 메모리 누수를 시뮬레이션한다.
- `jvm.memory.used` 메트릭이 지속적으로 증가하는 것을 확인할 수 있다.

### 커넥션 풀 누수

```java
@GetMapping("/jdbc")
public String jdbc() throws SQLException {
    Connection connection = dataSource.getConnection();
    log.info("connection info = {}", connection);
    // connection.close(); → 커넥션을 닫지 않는다!
    return "ok";
}
```

- 커넥션을 반환하지 않아서 호출할 때마다 활성 커넥션이 증가한다.
- `jdbc.connections.active` 메트릭이 계속 올라가고, 최대 커넥션에 도달하면 요청이 대기하거나 실패한다.

---

## 전체 흐름 정리

```
[마이크로미터 구조]

애플리케이션 메트릭 (CPU, JVM, 커넥션, HTTP 요청 등)
  │
  ▼
마이크로미터 (추상화 계층)
  │  메트릭을 표준 방법으로 수집
  │  SLF4J처럼 구현체를 갈아끼울 수 있음
  ▼
모니터링 툴 구현체 (프로메테우스, Datadog, New Relic 등)
  │
  ▼
모니터링 대시보드 (그라파나, 핀포인트 등)
  │  시각화 및 알림

[주요 메트릭]

JVM (jvm.)        → 메모리, GC, 스레드, 클래스
시스템 (system.)   → CPU, 디스크, 가동 시간
MVC (http.)       → 요청 수, 상태코드, URI별 필터링
데이터소스 (jdbc.) → 커넥션 풀 상태
로그 (logback.)   → 레벨별 로그 수
톰캣 (tomcat.)    → 쓰레드, 세션 (mbeanregistry 활성화 필요)
```
