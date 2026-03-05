# 스프링 부트 자동 구성 (Auto Configuration)

스프링 부트의 자동 구성이 무엇인지, 왜 필요한지를 이해하고, `@Conditional` 어노테이션과 순수 라이브러리/스프링 부트 라이브러리의 차이를 정리한다.

---

## 1. 순수 자바에서의 Configuration

스프링 부트 없이 DataSource, JdbcTemplate, TransactionManager를 사용하려면 직접 빈으로 등록해야 한다.

```java
@Configuration
public class DbConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public TransactionManager transactionManager() {
        return new JdbcTransactionManager(dataSource());
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}
```

- `@Configuration`을 붙이고 `@Bean`으로 하나하나 등록해야 한다.
- 어떤 빈이 필요한지 개발자가 직접 파악하고 설정해야 한다.

---

## 2. @Configuration을 삭제해도 되는 이유 - 스프링 부트 자동 구성

`DbConfig`에서 `@Configuration` 어노테이션을 **삭제**해도 DataSource, JdbcTemplate, TransactionManager가 빈으로 등록된다.

```java
//@Configuration  ← 삭제했는데도 빈이 등록됨
public class DbConfig {
    @Bean
    public DataSource dataSource() { ... }
    @Bean
    public TransactionManager transactionManager() { ... }
    @Bean
    public JdbcTemplate jdbcTemplate() { ... }
}
```

```java
@SpringBootTest
class DbConfigTest {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TransactionManager transactionManager;

    @Test
    void checkBean(){
        // 모두 null이 아님 → 빈이 등록되어 있음!
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(transactionManager).isNotNull();
    }
}
```

### 이유: 스프링 부트의 Auto Configuration

스프링 부트는 **일반적으로 자주 사용하는 수많은 빈들을 자동으로 등록해주는 기능**을 제공한다. `spring-boot-starter`에 포함된 `spring-boot-autoconfigure` 라이브러리가 이 역할을 한다.

### JdbcTemplateAutoConfiguration 내부 구조

스프링 부트가 JdbcTemplate을 자동 등록하는 과정을 살펴보면:

```java
@AutoConfiguration(after = {DataSourceAutoConfiguration.class})
@ConditionalOnClass({DataSource.class, JdbcTemplate.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({JdbcProperties.class})
@Import({JdbcTemplateConfiguration.class, NamedParameterJdbcTemplateConfiguration.class})
public class JdbcTemplateAutoConfiguration {
}
```

| 어노테이션 | 역할 |
|-----------|------|
| `@AutoConfiguration` | 자동 구성을 사용하려면 이 어노테이션을 등록해야 한다. |
| `@ConditionalOnClass` | `DataSource`, `JdbcTemplate` 클래스가 있는 경우에만 동작한다. 없으면 설정이 모두 무효화되고 빈도 등록되지 않는다. |
| `@Import` | `JdbcTemplateConfiguration`을 추가로 읽어들인다. |

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({JdbcOperations.class})
class JdbcTemplateConfiguration {

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // ... 설정 적용
        return jdbcTemplate;
    }
}
```

- `@ConditionalOnMissingBean({JdbcOperations.class})` : `JdbcOperations` 빈이 없을 때만 동작한다. (`JdbcOperations`는 `JdbcTemplate`의 부모 인터페이스)
- **개발자가 직접 빈을 등록하면 개발자의 빈을 사용하고, 자동 구성은 동작하지 않는다.**

---

## 3. @Conditional 어노테이션

`@Conditional`은 **특정 조건을 만족할 때만 빈을 등록**하는 기능이다. if문과 유사하다.

### 커스텀 Condition 구현

```java
public class MemoryCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // -Dmemory=on
        String memory = context.getEnvironment().getProperty("memory");
        return "on".equals(memory);
    }
}
```

- `Condition` 인터페이스를 구현한다.
- `matches()` 메서드가 `true`를 반환하면 조건에 만족해서 빈이 등록되고, `false`를 반환하면 등록되지 않는다.
- 위 예제는 VM 옵션 `-Dmemory=on`이 있을 때만 `true`를 반환한다.

### @Conditional 적용

```java
@Configuration
@Conditional(MemoryCondition.class)
public class MemoryConfig {

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}
```

- `-Dmemory=on` → `MemoryCondition.matches()`가 `true` → 빈 등록됨
- `-Dmemory=on` 없음 → `MemoryCondition.matches()`가 `false` → 빈 등록 안됨, 기능 동작 안함

---

## 4. @ConditionalOnXxx - 스프링 부트가 제공하는 다양한 Conditional

스프링 부트는 `@Conditional`을 직접 구현하지 않아도 되도록 다양한 `@ConditionalOnXxx` 어노테이션을 제공한다.

```java
@Configuration
// @Conditional(MemoryCondition.class)  ← 직접 구현할 필요 없음
@ConditionalOnProperty(name = "memory", havingValue = "on")  // 스프링 부트 제공
public class MemoryConfig2 {

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}
```

### 주요 @ConditionalOnXxx 목록

| 어노테이션 | 조건 |
|-----------|------|
| `@ConditionalOnClass` / `@ConditionalOnMissingClass` | 클래스가 있는 경우 / 없는 경우 동작 |
| `@ConditionalOnBean` / `@ConditionalOnMissingBean` | 빈이 등록되어 있는 경우 / 없는 경우 동작 |
| `@ConditionalOnProperty` | 환경 정보(프로퍼티)가 있는 경우 동작 |
| `@ConditionalOnResource` | 리소스가 있는 경우 동작 |
| `@ConditionalOnWebApplication` / `@ConditionalOnNotWebApplication` | 웹 애플리케이션인 경우 / 아닌 경우 동작 |
| `@ConditionalOnExpression` | SpEL 표현식에 만족하는 경우 동작 |

> `@Conditional`은 **스프링 프레임워크** 제공, `@ConditionalOnXxx`는 **스프링 부트** 제공

---

## 5. @Import와 ImportSelector - 자동 구성의 동작 원리

스프링 부트의 자동 구성이 내부적으로 어떻게 동작하는지 이해하려면, `@Import`의 두 가지 방식을 알아야 한다.

### 정적인 방식 - @Import(설정클래스.class)

```java
@Configuration
@Import(HelloConfig.class)  // HelloConfig를 직접 지정
public static class StaticConfig {
}
```

```java
@Configuration
public class HelloConfig {
    @Bean
    public HelloBean helloBean() {
        return new HelloBean();
    }
}
```

- `@Import`에 설정 클래스를 **직접 지정**한다.
- 코드에 대상이 딱 박혀 있어서, 설정을 변경하려면 코드를 수정해야 한다.

### 동적인 방식 - @Import(ImportSelector.class)

```java
public class HelloImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{"hello.selector.HelloConfig"};
    }
}
```

```java
@Configuration
@Import(HelloImportSelector.class)  // ImportSelector를 지정
public static class SelectorConfig {
}
```

- `ImportSelector` 인터페이스를 구현하면 설정 정보를 **동적으로 선택**할 수 있다.
- `selectImports()`가 반환하는 문자열 배열이 설정 클래스로 사용된다.
- 여기에 설정 정보로 사용할 클래스를 **프로그래밍으로 동적 결정**할 수 있다.

### 스프링 부트 자동 구성의 동작 순서

스프링 부트는 바로 이 `ImportSelector`의 동적 방식을 사용한다.

```
@SpringBootApplication
  └─ @EnableAutoConfiguration
       └─ @Import(AutoConfigurationImportSelector.class)
            └─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
                 파일을 열어서 설정 정보 선택
                 └─ 해당 파일의 설정 정보가 스프링 컨테이너에 등록되고 사용
```

| 단계 | 동작 |
|------|------|
| `@SpringBootApplication` | `@EnableAutoConfiguration`을 포함하고 있다. |
| `@EnableAutoConfiguration` | `@Import(AutoConfigurationImportSelector.class)`를 포함하고 있다. |
| `AutoConfigurationImportSelector` | `ImportSelector`의 구현체. `AutoConfiguration.imports` 파일을 읽어서 설정 클래스 목록을 반환한다. |
| `AutoConfiguration.imports` | 자동 구성 대상 클래스가 나열된 파일. 여기에 등록된 클래스가 스프링 컨테이너에 등록된다. |

> 결국 `memory-v2`에서 `AutoConfiguration.imports` 파일에 `memory.MemoryAutoConfig`를 적은 것이 이 흐름에 의해 자동으로 읽히는 것이다.

---

## 6. 순수 라이브러리 만들기 - memory-v1

`memory-v1`은 스프링 부트의 자동 구성 없이, 순수 자바 라이브러리로 만든 프로젝트이다.

### 프로젝트 구조

```
memory-v1/
  ├─ build.gradle
  └─ src/main/java/memory/
       ├─ Memory.java           ← 메모리 정보 데이터 클래스
       ├─ MemoryFinder.java     ← JVM 메모리 조회
       └─ MemoryController.java ← /memory REST 엔드포인트
```

### build.gradle

```gradle
plugins {
    id 'java'
}

group = 'memory'
sourceCompatibility = '17'

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web:3.0.2'
}
```

- 스프링 부트 플러그인 없이 `java` 플러그인만 사용한다.
- 빌드하면 `memory-v1.jar`가 생성된다.

### 핵심 클래스

```java
public class MemoryFinder {
    public Memory get() {
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        return new Memory(used, max);
    }
}
```

```java
@RestController
@RequiredArgsConstructor
public class MemoryController {
    private final MemoryFinder memoryFinder;

    @GetMapping("/memory")
    public Memory system() {
        Memory memory = memoryFinder.get();
        return memory;
    }
}
```

### 특징

- `memory-v1.jar`는 **스스로 동작하지 못한다.** 다른 프로젝트에 포함되어서 동작하는 라이브러리이다.
- 자동 구성이 없으므로, 사용하는 쪽에서 **빈을 직접 등록**해야 한다.

---

## 7. 순수 라이브러리 적용 - project-v1

`memory-v1.jar`를 실제 스프링 부트 프로젝트에서 사용한다.

### 라이브러리 추가

```gradle
dependencies {
    implementation files('libs/memory-v1.jar')  // 외부 라이브러리 추가
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

- `libs/` 폴더에 `memory-v1.jar`를 넣고 `files()`로 의존성을 추가한다.

### 빈을 직접 등록해야 한다

```java
@Configuration
public class MemoryConfig {

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }
}
```

- 라이브러리 내부의 `MemoryFinder`, `MemoryController`를 **개발자가 직접** `@Configuration`으로 빈 등록해야 한다.

### 문제점

라이브러리를 사용하는 개발자 입장에서:
- 라이브러리 내부에 있는 **어떤 빈을 등록해야 하는지** 알아야 한다.
- 그것을 **하나하나 빈으로 등록**해야 한다.
- 간단한 라이브러리가 아니라 초기 설정이 복잡하다면 상당히 귀찮은 작업이 된다.

> 이 문제를 자동으로 처리해주는 것이 **스프링 부트 자동 구성(Auto Configuration)** 이다.

---

## 8. 스프링 부트 라이브러리 만들기 - memory-v2

`memory-v2`는 스프링 부트 자동 구성을 적용한 라이브러리이다. 사용하는 쪽에서 빈을 직접 등록할 필요가 없다.

### memory-v1 대비 추가된 것

```
memory-v2/
  ├─ src/main/java/memory/
  │    ├─ Memory.java
  │    ├─ MemoryFinder.java
  │    ├─ MemoryController.java
  │    └─ MemoryAutoConfig.java         ← 자동 구성 클래스 추가
  └─ src/main/resources/META-INF/spring/
       └─ org.springframework.boot.autoconfigure.AutoConfiguration.imports  ← 자동 구성 대상 지정
```

### MemoryAutoConfig - 자동 구성 클래스

```java
@AutoConfiguration
@ConditionalOnProperty(name = "memory", havingValue = "on")
public class MemoryAutoConfig {

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}
```

- `@AutoConfiguration` : 자동 구성 클래스임을 선언한다.
- `@ConditionalOnProperty` : `memory=on` 프로퍼티가 있을 때만 빈을 등록한다.
- 빈 등록 로직을 **라이브러리 안에 포함**시켰다.

### 자동 구성 대상 지정

여기서 끝이 아니다. 스프링 부트에게 이 클래스가 자동 구성 대상임을 알려줘야 한다.

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
memory.MemoryAutoConfig
```

- 스프링 부트는 시작 시점에 이 파일을 읽어서 자동 구성으로 사용한다.

---

## 9. 스프링 부트 라이브러리 프로젝트에 적용 - project-v2

`memory-v2.jar`를 적용하는 프로젝트이다. project-v1과 비교하면 차이가 극명하다.

### 라이브러리 추가

```gradle
dependencies {
    implementation files('libs/memory-v2.jar')  // 외부 라이브러리 추가
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### MemoryConfig가 필요 없다

```java
@SpringBootApplication
public class ProjectV2Application {
    public static void main(String[] args) {
        SpringApplication.run(ProjectV2Application.class, args);
    }
}
```

- `@Configuration`으로 빈을 직접 등록하는 코드가 **없다.**
- `memory-v2.jar` 안의 `MemoryAutoConfig`가 자동으로 빈을 등록해준다.
- `memory=on` 프로퍼티만 설정하면 `/memory` 엔드포인트가 자동으로 활성화된다.

### project-v1 vs project-v2 비교

| | project-v1 (순수 라이브러리) | project-v2 (스프링 부트 라이브러리) |
|---|---------------------------|-------------------------------|
| **사용 라이브러리** | `memory-v1.jar` | `memory-v2.jar` |
| **빈 등록** | `MemoryConfig`를 직접 작성해야 함 | **자동** (라이브러리가 알아서 등록) |
| **라이브러리 내부 지식** | 어떤 빈이 필요한지 알아야 함 | 몰라도 됨 |
| **활성화 방법** | `@Configuration` 클래스 작성 | `memory=on` 프로퍼티 설정 |

---

## 전체 흐름 정리

```
[자동 구성 이해]

순수 @Configuration으로 빈 직접 등록 (DbConfig)
  │  문제: 매번 개발자가 직접 설정해야 함
  ▼
@Configuration 삭제해도 빈이 등록됨
  │  이유: 스프링 부트 Auto Configuration이 자동으로 등록
  │  핵심: @ConditionalOnMissingBean → 개발자 빈이 있으면 자동 구성 안함
  ▼
@Conditional로 조건부 등록
  │  matches()가 true일 때만 빈 등록 (if문과 유사)
  ▼
@ConditionalOnXxx (스프링 부트 제공)
     @ConditionalOnProperty, @ConditionalOnClass 등


[라이브러리 만들기]

순수 라이브러리 (memory-v1)
  │  빈 등록 코드가 없음 → 사용하는 쪽에서 직접 등록해야 함
  ▼
순수 라이브러리 적용 (project-v1)
  │  MemoryConfig를 직접 작성해서 빈 등록
  │  문제: 라이브러리 내부를 알아야 하고, 설정이 번거로움
  ▼
스프링 부트 라이브러리 (memory-v2)
  │  @AutoConfiguration + META-INF 파일로 자동 구성 제공
  ▼
스프링 부트 라이브러리 적용 (project-v2)
     빈 등록 코드 없이 라이브러리만 추가하면 자동으로 동작
```

> 스프링 부트의 자동 구성 덕분에 라이브러리를 만드는 쪽에서 빈 등록 로직을 제공하고, 사용하는 쪽에서는 라이브러리를 추가하기만 하면 된다. 이것이 `spring-boot-starter-xxx`가 한 줄로 동작하는 원리이다.
