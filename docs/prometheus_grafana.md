# 프로메테우스와 그라파나

애플리케이션에서 발생한 메트릭을 그 순간만 확인하는 것이 아니라 과거 이력까지 함께 확인하려면 메트릭을 보관하는 DB가 필요하다. 프로메테우스가 이 역할을 담당하고, 그라파나는 프로메테우스에 저장된 데이터를 그래프로 시각화해주는 대시보드 툴이다.

---

## 전체 구조

```
스프링 부트 액추에이터 + 마이크로미터
  │  수많은 메트릭을 자동 생성
  │  프로메테우스 포맷으로 변환 (/actuator/prometheus)
  ▼
프로메테우스 (메트릭 DB)
  │  주기적으로 애플리케이션의 메트릭을 수집 (Pull 방식)
  │  수집한 메트릭을 내부 DB에 저장
  ▼
그라파나 (대시보드)
     프로메테우스에서 데이터를 조회
     그래프로 시각화하여 사용자에게 제공
```

| 역할 | 도구 | 설명 |
|------|------|------|
| 메트릭 생성 | 마이크로미터 + 액추에이터 | 애플리케이션 메트릭을 프로메테우스 포맷으로 변환 |
| 메트릭 수집/저장 | 프로메테우스 | 주기적으로 메트릭을 수집하여 시계열 DB에 저장 |
| 시각화 | 그라파나 | 프로메테우스 데이터를 그래프 대시보드로 표시 |

---

## 1. 프로메테우스 설치

https://prometheus.io/download/ 에서 본인에게 맞는 OS를 선택한다. (Mac OS는 darwin 선택)

```bash
# tar 압축 해제 후 실행
./prometheus
```

- 실행 후 http://localhost:9090 에서 프로메테우스 UI에 접근할 수 있다.

---

## 2. 애플리케이션 설정

프로메테우스가 애플리케이션의 메트릭을 가져가려면, 프로메테우스가 이해할 수 있는 포맷으로 메트릭을 제공해야 한다.

### 의존성 추가

```gradle
dependencies {
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

- 이 의존성을 추가하면 액추에이터에 `/actuator/prometheus` 엔드포인트가 자동으로 추가된다.
- 프로메테우스는 `/actuator/metrics`의 JSON 포맷은 이해하지 못한다. 마이크로미터가 프로메테우스 전용 포맷으로 변환해준다.

### 포맷 비교

```
# /actuator/metrics (JSON) → 프로메테우스가 이해 못함
{"name":"jvm.memory.used","measurements":[{"statistic":"VALUE","value":1.234}]}

# /actuator/prometheus (프로메테우스 포맷) → 프로메테우스가 수집 가능
jvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 1234.0
```

---

## 3. 프로메테우스 설정

프로메테우스가 애플리케이션의 `/actuator/prometheus`를 주기적으로 호출하여 메트릭을 수집하도록 설정한다.

### prometheus.yml 수정

프로메테우스 설치 폴더의 `prometheus.yml` 파일에 다음을 추가한다.

```yaml
scrape_configs:
  # 기존 설정 유지...

  # 추가
  - job_name: "spring-actuator"
    metrics_path: '/actuator/prometheus'
    scrape_interval: 1s
    static_configs:
      - targets: ['localhost:8080']
```

| 설정 | 설명 |
|------|------|
| `job_name` | 수집 작업의 이름 |
| `metrics_path` | 메트릭을 가져올 엔드포인트 경로 |
| `scrape_interval` | 수집 주기 (1초마다 호출) |
| `targets` | 수집 대상 애플리케이션의 호스트:포트 |

- 설정 후 프로메테우스 서버를 재시작해야 적용된다.
- 프로메테우스는 1초에 한 번씩 `http://localhost:8080/actuator/prometheus`를 호출해서 메트릭을 수집한다.

---

## 4. 프로메테우스를 통한 데이터 조회

프로메테우스 UI(http://localhost:9090)에서 수집된 메트릭을 조회할 수 있다.

### 기본 조회 예시

| 쿼리 | 설명 |
|------|------|
| `jvm_memory_used_bytes` | JVM 메모리 사용량 |
| `http_server_requests_seconds_count` | HTTP 요청 수 |
| `system_cpu_usage` | 시스템 CPU 사용률 |
| `jdbc_connections_active` | 활성 DB 커넥션 수 |
| `logback_events_total` | 로그 이벤트 수 |

### 메트릭 이름 규칙

마이크로미터의 메트릭 이름과 프로메테우스에서 사용하는 이름이 다르다.

| 마이크로미터 (액추에이터) | 프로메테우스 |
|--------------------------|-------------|
| `jvm.memory.used` | `jvm_memory_used_bytes` |
| `http.server.requests` | `http_server_requests_seconds_count` |
| `system.cpu.usage` | `system_cpu_usage` |

- `.`은 `_`로 변환된다.
- 단위 정보가 접미사로 붙는다. (`_bytes`, `_seconds` 등)

---

## 전체 흐름 정리

```
[설정 과정]

1. 애플리케이션 설정
   │  micrometer-registry-prometheus 의존성 추가
   │  → /actuator/prometheus 엔드포인트 자동 활성화
   ▼
2. 프로메테우스 설정
   │  prometheus.yml에 수집 대상 추가
   │  → 주기적으로 /actuator/prometheus 호출
   ▼
3. 데이터 조회
   │  프로메테우스 UI (localhost:9090)에서 쿼리
   ▼
4. 시각화 (그라파나)
      프로메테우스를 데이터소스로 연결
      대시보드에서 그래프로 확인
```
