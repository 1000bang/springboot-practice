# 외부 설정 조회와 활용

외부 설정값을 코드에서 읽어오는 다양한 방법(Environment, @Value, @ConfigurationProperties)과, YAML 설정 파일, 그리고 @Profile을 활용한 환경별 빈 등록을 정리한다.

---

## 1. 외부 설정 조회 - Environment

`Environment`를 직접 주입받아 `getProperty()`로 값을 꺼내는 방식이다.

```java
@Configuration
public class MyDataSourceEnvConfig {

    private final Environment env;

    public MyDataSourceEnvConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public MyDataSource myDataSource() {
        String url = env.getProperty("my.datasource.url");
        String username = env.getProperty("my.datasource.username");
        String password = env.getProperty("my.datasource.password");
        int maxConnection = env.getProperty("my.datasource.etc.max-connection", Integer.class);
        Duration timeout = env.getProperty("my.datasource.etc.timeout", Duration.class);
        List<String> options = env.getProperty("my.datasource.etc.options", List.class);

        return new MyDataSource(url, username, password, maxConnection, timeout, options);
    }
}
```

- 외부 설정 방식이 달라져도(OS 환경변수, 시스템 속성, properties 파일 등) 애플리케이션 코드를 그대로 유지할 수 있다.
- `getProperty(key, Type.class)`로 타입 변환도 가능하다.

### 단점

- `env.getProperty(key)`를 통해 값을 하나하나 꺼내는 과정을 반복해야 한다.

---

## 2. 외부 설정 조회 - @Value

`@Value`를 사용하면 외부 설정값을 필드나 파라미터에 직접 주입받을 수 있다.

### 필드 주입 방식

```java
@Configuration
public class MyDataSourceValueConfig {

    @Value("${my.datasource.url}")
    private String url;
    @Value("${my.datasource.username}")
    private String username;
    @Value("${my.datasource.password}")
    private String password;
    @Value("${my.datasource.etc.max-connection:2}")
    private int maxConnection;
    @Value("${my.datasource.etc.timeout}")
    private Duration timeout;
    @Value("${my.datasource.etc.options}")
    private List<String> options;

    @Bean
    public MyDataSource myDataSource() {
        return new MyDataSource(url, username, password, maxConnection, timeout, options);
    }
}
```

### 파라미터 주입 방식

```java
@Bean
public MyDataSource myDataSource2(
        @Value("${my.datasource.url}") String url,
        @Value("${my.datasource.username}") String username,
        @Value("${my.datasource.password}") String password,
        @Value("${my.datasource.etc.max-connection}") int maxConnection,
        @Value("${my.datasource.etc.timeout}") Duration timeout,
        @Value("${my.datasource.etc.options}") List<String> options) {

    return new MyDataSource(url, username, password, maxConnection, timeout, options);
}
```

- `@Value`도 내부에서는 `Environment`를 사용한다.
- 타입 변환을 자동으로 해준다.
- `:` 를 사용해서 기본값을 지정할 수 있다. (`${key:기본값}` → 해당 값이 없으면 기본값 사용)

### 단점

- `@Value`로 하나하나 키 값을 입력하고 주입받아야 하는 부분이 번거롭다.
- 설정 데이터는 `my.datasource`처럼 정보의 묶음으로 되어 있는데, 이를 객체로 변환해서 사용할 수 있다면 더 편리할 것이다.

---

## 3. 외부 설정 조회 - @ConfigurationProperties

`@ConfigurationProperties`를 사용하면 외부 설정의 묶음 정보를 **객체로 변환**해서 사용할 수 있다.

### 설정 속성 객체

```java
@Data
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV1 {

    private String url;
    private String username;
    private String password;
    private Etc etc;

    @Data
    public static class Etc {
        private int maxConnection;
        private Duration timeout;
        private List<String> options = new ArrayList<>();
    }
}
```

- `@ConfigurationProperties("my.datasource")` : `my.datasource`로 시작하는 외부 설정을 이 객체에 바인딩한다.
- 중첩 객체(`Etc`)도 지원한다. `my.datasource.etc.max-connection` → `etc.maxConnection`에 매핑된다.

### 설정 클래스에서 사용

```java
@EnableConfigurationProperties(MyDataSourcePropertiesV1.class)
public class MyDataSourceConfigV1 {

    private final MyDataSourcePropertiesV1 properties;

    public MyDataSourceConfigV1(MyDataSourcePropertiesV1 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource dataSource() {
        return new MyDataSource(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getEtc().getMaxConnection(),
                properties.getEtc().getTimeout(),
                properties.getEtc().getOptions()
        );
    }
}
```

### 빈 등록 방법

| 방법 | 설명 |
|------|------|
| `@EnableConfigurationProperties(클래스.class)` | 해당 클래스를 직접 지정하여 빈으로 등록 |
| `@ConfigurationPropertiesScan` | 컴포넌트 스캔처럼 자동으로 탐색하여 빈으로 등록 |

- `@ConfigurationProperties`만 붙여서는 빈으로 등록되지 않는다. 위 두 가지 중 하나를 사용해야 한다.
- 타입 안전한 설정 속성을 사용할 수 있다. `maxConnection`에 문자를 입력하면 오류가 발생해서 실수를 방지해준다.

---

## 4. 외부 설정 사용 - @ConfigurationProperties 생성자

Setter 방식은 설정값이 변경될 위험이 있으므로, **생성자를 통해 설정 정보를 주입**하는 방식이 더 안전하다.

```java
@Getter
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV2 {

    private String url;
    private String username;
    private String password;
    private Etc etc;

    public MyDataSourcePropertiesV2(String url, String username, String password,
                                     @DefaultValue Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    @Getter
    public static class Etc {
        private int maxConnection;
        private Duration timeout;
        private List<String> options;

        public Etc(int maxConnection, Duration timeout,
                   @DefaultValue("DEFAULT") List<String> options) {
            this.maxConnection = maxConnection;
            this.timeout = timeout;
            this.options = options;
        }
    }
}
```

- `@Getter`만 사용하고 `@Setter`는 제거했다. 생성 이후 값을 변경할 수 없다.
- `@DefaultValue` : 해당 값을 찾을 수 없는 경우 기본값을 사용한다.

### @ConstructorBinding 참고

| 스프링 부트 버전 | 동작 |
|-----------------|------|
| 3.0 이전 | 생성자 바인딩 시 `@ConstructorBinding` 필수 |
| 3.0 이후 | 생성자가 하나일 때는 생략 가능 |
| 생성자가 둘 이상 | 사용할 생성자에 `@ConstructorBinding` 적용 |

---

## 5. 외부 설정 사용 - @ConfigurationProperties 검증

타입과 객체를 통해 숫자에 문자가 들어오는 문제는 해결되었지만, `max-connection`의 값을 `0`으로 설정하면 커넥션이 하나도 만들어지지 않는 문제가 발생한다. **자바 빈 검증기(Bean Validation)** 를 사용하면 이런 문제를 해결할 수 있다.

```java
@Getter
@ConfigurationProperties("my.datasource")
@Validated
public class MyDataSourcePropertiesV3 {

    @NotEmpty
    private String url;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @Valid
    private Etc etc;

    public MyDataSourcePropertiesV3(String url, String username, String password,
                                     @DefaultValue Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    @Getter
    public static class Etc {
        @Min(1) @Max(999)
        private int maxConnection;

        @DurationMin(seconds = 1)
        @DurationMax(seconds = 60)
        private Duration timeout;

        private List<String> options;

        public Etc(int maxConnection, Duration timeout,
                   @DefaultValue("DEFAULT") List<String> options) {
            this.maxConnection = maxConnection;
            this.timeout = timeout;
            this.options = options;
        }
    }
}
```

- `@Validated` : 이 어노테이션을 붙여야 자바 빈 검증기가 동작한다.
- `@Valid` : 스프링 부트 3.4 이상에서 중첩 필드에 유효성 검사를 적용하려면 필요하다.

### 주요 검증 어노테이션

| 패키지 | 어노테이션 | 설명 |
|--------|-----------|------|
| `jakarta.validation` | `@NotEmpty` | 빈 문자열 불가 |
| `jakarta.validation` | `@Min`, `@Max` | 숫자 최소/최대값 |
| `org.hibernate.validator` | `@DurationMin`, `@DurationMax` | Duration 최소/최대값 |

- `jakarta.validation`으로 시작하는 것은 자바 표준 검증기에서 지원하는 기능이다.
- `org.hibernate.validator`로 시작하는 것은 하이버네이트 검증기(표준 검증기의 구현체)에서 직접 제공하는 기능이다.

### 검증 실패 시 오류 메시지 예시

```
Property: my.datasource.etc.maxConnection
Value: "0"
Origin: class path resource [application.properties] - 5:34
Reason: 1 이상이어야 합니다
```

- 애플리케이션 로딩 시점에 예외가 발생하므로 잘못된 설정으로 운영되는 것을 방지할 수 있다.

---

## 6. YAML - application.yml

YAML은 properties보다 계층 구조를 표현하기 좋아서 설정 파일로 많이 사용된다.

### properties 방식

```properties
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_pw
my.datasource.etc.max-connection=1
my.datasource.etc.timeout=3500ms
my.datasource.etc.options=CACHE,ADMIN
```

### YAML 방식

```yaml
my:
  datasource:
    url: local.db.com
    username: local_user
    password: local_pw
    etc:
      max-connection: 1
      timeout: 60s
      options: ADMIN, CACHE
---
spring:
  config:
    activate:
      on-profile: dev
my:
  datasource:
    url: dev.db.com
    username: dev_user
    password: dev_pw
    etc:
      max-connection: 1
      timeout: 60s
      options: DEV, CACHE
---
spring:
  config:
    activate:
      on-profile: prd
my:
  datasource:
    url: prd.db.com
    username: prd_user
    password: prd_pw
    etc:
      max-connection: 50
      timeout: 10s
      options: PROD, CACHE
```

| | properties | YAML |
|---|-----------|------|
| **구조 표현** | `my.datasource.url=value` (flat) | 들여쓰기로 계층 표현 |
| **문서 구분자** | `#---` | `---` |
| **가독성** | 키가 길어지면 읽기 어려움 | 계층이 보여서 직관적 |
| **주의사항** | - | 들여쓰기에 공백을 사용해야 함 (탭 불가) |

- properties에서 dash 표기(`max-connection`)는 자동으로 camelCase(`maxConnection`)에 매핑된다. (relaxed binding)

---

## 7. @Profile - 환경별 빈 등록

`@Profile`을 사용하면 프로필에 따라 서로 다른 빈을 등록할 수 있다. 설정값을 바꾸는 것이 아니라, **빈 자체를 다르게 등록**해야 할 때 사용한다.

### 예시: 결제 시스템

로컬 환경에서 실제 결제가 발생하면 안 되므로, 환경에 따라 다른 구현체를 주입해야 한다.

```java
public interface PayClient {
    void pay(int money);
}
```

```java
public class LocalPayClient implements PayClient {
    @Override
    public void pay(int money) {
        log.info("local 결제 money={}", money);
    }
}
```

```java
public class ProdPayClient implements PayClient {
    @Override
    public void pay(int money) {
        log.info("prod money = {}", money);
    }
}
```

### @Profile로 빈 분기

```java
@Configuration
public class PayConfig {

    @Bean
    @Profile("default")
    public LocalPayClient localPayClient() {
        log.info("localPayClient 빈 등록");
        return new LocalPayClient();
    }

    @Bean
    @Profile("prd")
    public ProdPayClient prodPayClient() {
        log.info("prodPayClient 빈 등록");
        return new ProdPayClient();
    }
}
```

- `@Profile("default")` : 프로필을 지정하지 않으면 `default` 프로필로 실행되므로 `LocalPayClient`가 등록된다.
- `@Profile("prd")` : `--spring.profiles.active=prd`로 실행하면 `ProdPayClient`가 등록된다.

### 사용하는 쪽

```java
@Service
public class OrderService {

    private final PayClient payClient;

    public OrderService(PayClient payClient) {
        this.payClient = payClient;
    }

    public void order(int money) {
        payClient.pay(money);
    }
}
```

- `OrderService`는 `PayClient` 인터페이스에만 의존한다. 어떤 구현체가 주입되는지는 프로필에 의해 결정된다.
- `ApplicationRunner`를 구현한 `OrderRunner`가 애플리케이션 시작 시 `orderService.order(1000)`을 호출하여 동작을 확인한다.

---

## 전체 흐름 정리

```
[외부 설정 조회 방법의 발전]

Environment (env.getProperty("key"))
  │  직접 주입받아 하나하나 꺼내야 함
  ▼
@Value ("${key}")
  │  필드/파라미터에 주입, 기본값 지정 가능
  │  여전히 키를 하나하나 지정해야 함
  ▼
@ConfigurationProperties (Setter 방식)
  │  설정 묶음을 객체로 변환, 타입 안전
  │  Setter로 값이 변경될 위험이 있음
  ▼
@ConfigurationProperties (생성자 방식)
  │  불변 객체로 안전하게 사용
  │  값의 범위 검증이 안됨
  ▼
@ConfigurationProperties + @Validated (검증)
     자바 빈 검증기로 값의 범위까지 검증
     잘못된 설정 시 애플리케이션 로딩 실패

[설정 파일]
properties → YAML (계층 구조, 가독성)

[@Profile]
설정값이 아닌 빈 자체를 환경별로 다르게 등록
@Profile("default") → LocalPayClient
@Profile("prd") → ProdPayClient
```

---

## 8. 민감한 설정값 암호화 - Jasypt

DB 비밀번호 등 민감한 값이 Git에 노출되는 것을 방지하기 위해 설정값을 암호화해서 관리할 수 있다. 실무에서는 AWS KMS, Secrets Manager 등 인프라 레벨의 시크릿 관리를 가장 많이 사용하지만, 인프라 없이 애플리케이션 레벨에서 암호화하려면 Jasypt를 활용할 수 있다.

### 민감 정보 관리 방식 비교

| 방식 | 복잡도 | 적합한 상황 |
|------|--------|------------|
| 환경변수 + 시크릿 관리 도구 (AWS KMS 등) | 낮음 | 대부분의 프로젝트 (가장 보편적) |
| Jasypt | 중간 | properties 파일에 값을 남겨야 하는 경우 |
| Spring Cloud Config + Vault | 높음 | 대규모 MSA, 중앙 집중 설정 관리 |

### 의존성 추가

```gradle
dependencies {
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5'
}
```

### Jasypt 설정

```java
@Configuration
public class JasyptConfig {

    @Value("${jasypt.encryptor.password}")
    private String password;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}
```

- 빈 이름을 `jasyptStringEncryptor`로 지정해야 스타터가 이 빈을 사용한다.
- `jasypt.encryptor.password`는 암/복호화에 사용되는 키로, **절대 properties 파일에 넣지 않는다.** 실행 시 외부에서 주입한다.

### 암호화된 값을 설정 파일에 적용

```yaml
test:
  jasypt:
    value: ENC(XCE9U6MP/DW9LrxNCTEkzw==)
```

- `ENC(...)` 로 감싸면 Jasypt가 자동으로 복호화해서 원본 값을 주입한다.
- 복호화된 값은 `@Value`나 `Environment`로 평소처럼 사용하면 된다.

```java
@Component
public class JasyptRunner implements ApplicationRunner {

    @Value("${test.jasypt.value}")
    String jasyptValue;  // 복호화된 원본 값이 주입됨

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(jasyptValue);
    }
}
```

### 암호화 키 전달 방법

암호화 키는 반드시 **실행 시 외부에서 전달**해야 한다. properties에 넣으면 암호화의 의미가 없다.

```bash
# 커맨드 라인 옵션 인수
java -jar app.jar --jasypt.encryptor.password=mySecretKey

# 자바 시스템 속성
java -Djasypt.encryptor.password=mySecretKey -jar app.jar

# 환경변수
export JASYPT_ENCRYPTOR_PASSWORD=mySecretKey
java -jar app.jar
```

IntelliJ에서는 **Run Configuration > VM options**에 `-Djasypt.encryptor.password=mySecretKey`를 추가한다.

### 암호화 값 생성 - 테스트 코드

설정 파일에 넣을 `ENC(...)` 값을 생성하려면 테스트 코드로 암호화한다.

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JasyptConfig.class)
public class JasyptTest {

    @Autowired
    private StringEncryptor jasyptEncryptor;

    @Test
    void custom_jasypt_test() {
        String encrypted = jasyptEncryptor.encrypt("test");
        System.out.println("encrypted: " + encrypted);

        String decrypted = jasyptEncryptor.decrypt(encrypted);
        System.out.println("decrypted: " + decrypted);

        Assertions.assertThat(decrypted).isEqualTo("test");
    }
}
```

- 테스트 실행 시에도 VM options에 `-Djasypt.encryptor.password=mySecretKey`를 넣어야 한다.
- 출력된 `encrypted` 값을 `ENC(출력값)` 형태로 설정 파일에 넣으면 된다.
- **암호화할 때 사용한 키와 복호화할 때 사용하는 키가 반드시 동일해야 한다.**

### 주의: scanBasePackages

`@SpringBootApplication(scanBasePackages = {...})`를 사용하고 있다면 Jasypt 관련 패키지가 스캔 대상에 포함되어야 한다. 누락되면 커스텀 설정이 적용되지 않아 복호화에 실패한다.

```java
@SpringBootApplication(scanBasePackages = {"hello.datasource", "hello.pay", "hello.jasypt"})
```
