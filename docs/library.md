# 스프링 부트 라이브러리 관리

스프링 부트가 라이브러리를 어떻게 관리하는지, 직접 지정 방식부터 스타터까지 3단계 발전 과정을 정리한다.

---

## 문제: 웹 프로젝트에 필요한 라이브러리가 너무 많다

웹 프로젝트를 하나 시작하려면 다음 라이브러리들이 필요하다.

- 스프링 웹 MVC
- 내장 톰캣
- JSON 처리 (Jackson)
- 스프링 부트 관련
- 로깅 (Logback, SLF4J)
- YML 파서 (SnakeYAML)

이 라이브러리들의 버전을 하나하나 지정하고, 서로 호환되는 버전을 맞추는 것은 번거롭고 실수하기 쉽다.

---

## 1단계: 라이브러리 직접 지정

개발자가 모든 라이브러리와 버전을 직접 명시하는 방식이다.

```gradle
dependencies {
    // 스프링 웹 MVC
    implementation 'org.springframework:spring-webmvc:6.0.4'
    // 내장 톰캣
    implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.1.5'
    // JSON 처리
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.14.1'
    // 스프링 부트 관련
    implementation 'org.springframework.boot:spring-boot:3.0.2'
    implementation 'org.springframework.boot:spring-boot-autoconfigure:3.0.2'
    // LOG 관련
    implementation 'ch.qos.logback:logback-classic:1.4.5'
    implementation 'org.apache.logging.log4j:log4j-to-slf4j:2.19.0'
    implementation 'org.slf4j:jul-to-slf4j:2.0.6'
    // YML 관련
    implementation 'org.yaml:snakeyaml:1.33'
}
```

### 문제점

| 문제 | 설명 |
|------|------|
| **버전을 하나하나 지정해야 한다** | 라이브러리마다 호환되는 버전을 찾아서 직접 명시해야 한다. |
| **버전 호환성을 직접 맞춰야 한다** | 스프링 6.0.4에 톰캣 10.1.5가 호환되는지, Jackson 2.14.1이 맞는지 개발자가 확인해야 한다. |
| **버전 업그레이드가 어렵다** | 하나를 올리면 연쇄적으로 다른 라이브러리 버전도 함께 맞춰야 한다. |

---

## 2단계: 스프링 부트 라이브러리 버전 관리

`io.spring.dependency-management` 플러그인을 사용하면 **라이브러리 버전을 생략**할 수 있다. 스프링 부트가 부트 버전에 맞는 최적화된 라이브러리 버전을 선택해준다.

```gradle
plugins {
    id 'org.springframework.boot' version '3.0.2'
    id 'io.spring.dependency-management' version '1.1.0'  // 버전 관리 플러그인
    id 'java'
}

dependencies {
    // 스프링 웹 MVC
    implementation 'org.springframework:spring-webmvc'           // 버전 생략!
    // 내장 톰캣
    implementation 'org.apache.tomcat.embed:tomcat-embed-core'   // 버전 생략!
    // JSON 처리
    implementation 'com.fasterxml.jackson.core:jackson-databind'  // 버전 생략!
    // 스프링 부트 관련
    implementation 'org.springframework.boot:spring-boot'
    implementation 'org.springframework.boot:spring-boot-autoconfigure'
    // LOG 관련
    implementation 'ch.qos.logback:logback-classic'
    implementation 'org.apache.logging.log4j:log4j-to-slf4j'
    implementation 'org.slf4j:jul-to-slf4j'
    // YML 관련
    implementation 'org.yaml:snakeyaml'
}
```

### BOM (Bill of Materials)

스프링 부트는 내부적으로 **BOM(자재 명세서)** 을 통해 수백 개 라이브러리의 호환 버전을 미리 정의해두고 있다.

> BOM: 제품을 구성하는 모든 부품들에 대한 목록. 스프링 부트의 BOM에는 각 라이브러리에 대한 버전이 명시되어 있다.
>
> 참고: [spring-boot-dependencies/build.gradle](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-dependencies/build.gradle)

### 해결된 것과 남은 문제

| | 1단계 | 2단계 |
|---|-------|-------|
| **버전 지정** | 직접 명시 | 스프링 부트가 자동 선택 (생략 가능) |
| **버전 호환성** | 개발자가 확인 | 스프링 부트가 보장 |
| **라이브러리 나열** | 하나하나 직접 나열 | **여전히 하나하나 직접 나열해야 함** |

> 버전 문제는 해결되었지만, 웹 프로젝트를 시작하려면 여전히 어떤 라이브러리들이 필요한지 알고 있어야 하고 하나하나 나열해야 한다.

---

## 3단계: 스프링 부트 스타터

스프링 부트는 프로젝트를 시작하는데 필요한 관련 라이브러리를 모아둔 **스타터(Starter)** 를 제공한다.

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**이 한 줄이 1단계의 9개 라이브러리를 모두 포함한다.**

### spring-boot-starter-web이 포함하는 라이브러리

```
spring-boot-starter-web
  ├─ spring-boot-starter              ← 핵심 스타터 (자동 구성, 로깅, YAML)
  │    ├─ spring-boot
  │    ├─ spring-boot-autoconfigure
  │    ├─ logback-classic
  │    ├─ log4j-to-slf4j
  │    ├─ jul-to-slf4j
  │    └─ snakeyaml
  ├─ spring-webmvc                    ← 스프링 MVC
  ├─ tomcat-embed-core                ← 내장 톰캣
  └─ jackson-databind                 ← JSON 처리
```

### 자주 사용하는 스타터 목록

| 스타터 | 설명 |
|--------|------|
| `spring-boot-starter` | 핵심 스타터, 자동 구성, 로깅, YAML |
| `spring-boot-starter-web` | 웹 구축을 위한 스타터, RESTful, 스프링 MVC, 내장 톰캣 |
| `spring-boot-starter-jdbc` | JDBC, HikariCP 커넥션풀 |
| `spring-boot-starter-data-jpa` | 스프링 데이터 JPA, 하이버네이트 |
| `spring-boot-starter-data-mongodb` | 스프링 데이터 몽고 |
| `spring-boot-starter-data-redis` | 스프링 데이터 Redis, Lettuce 클라이언트 |
| `spring-boot-starter-thymeleaf` | 타임리프 뷰와 웹 MVC |
| `spring-boot-starter-validation` | 자바 빈 검증기 (하이버네이트 Validator) |
| `spring-boot-starter-batch` | 스프링 배치 |

---

## 3단계 비교 정리

```
1단계: 라이브러리 직접 지정
  │  9개 라이브러리를 버전까지 직접 나열
  │  문제: 버전 호환성을 개발자가 맞춰야 함
  ▼
2단계: 스프링 부트 버전 관리 (dependency-management)
  │  라이브러리를 나열하되, 버전은 생략 가능
  │  문제: 어떤 라이브러리가 필요한지는 여전히 알아야 함
  ▼
3단계: 스프링 부트 스타터 (spring-boot-starter-web)
     한 줄로 필요한 라이브러리를 모두 포함
```

| | 1단계 (직접 지정) | 2단계 (버전 관리) | 3단계 (스타터) |
|---|-------------------|-------------------|----------------|
| **버전 명시** | 직접 | 생략 가능 | 생략 가능 |
| **라이브러리 나열** | 하나하나 직접 | 하나하나 직접 | 한 줄로 해결 |
| **호환성 보장** | 개발자 책임 | 스프링 부트가 보장 | 스프링 부트가 보장 |
| **dependencies 줄 수** | 9줄 | 9줄 | 1줄 |

---

## 외부 라이브러리 버전 변경

스타터에 포함된 라이브러리에 취약점이 발견되었을 때, 스프링 부트가 관리하는 버전을 직접 변경할 수 있다.

```gradle
ext['tomcat.version'] = '10.1.4'
```

- 스프링 부트가 관리하는 라이브러리는 위처럼 속성 값으로 버전을 변경한다.
- 스프링 부트가 **관리하지 않는** 외부 라이브러리는 기존처럼 버전을 직접 명시해야 한다.

> 버전 변경에 필요한 속성 값 목록: [Spring Boot Dependency Versions Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/dependency-versions.html#appendix.dependency-versions.properties)
