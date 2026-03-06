# 외부 설정과 프로필

스프링 부트에서 외부 설정값을 주입하는 다양한 방법과, 프로필을 통한 환경별 설정 관리, 그리고 설정 간 우선순위를 정리한다.

---

## 1. OS 환경변수

`System.getenv()`를 통해 OS에 설정된 환경변수를 읽어온다.

```java
Map<String, String> env = System.getenv();
for (String key : env.keySet()) {
    log.info("env = {} = {}", key, System.getenv(key));
}
```

- OS 환경변수는 해당 OS를 사용하는 모든 프로세스에서 사용할 수 있는 전역 변수이다.
- 예: `PATH`, `JAVA_HOME` 등

---

## 2. 자바 시스템 속성

`System.getProperty()`를 통해 JVM에 전달된 `-D` 옵션 값을 읽어온다.

```java
// 실행 시: java -Durl=devdb.com -jar abc.jar
Properties properties = System.getProperties();
for (Object key : properties.keySet()) {
    log.info("prop {} = {}", key, System.getProperty(String.valueOf(key)));
}
```

- JVM 레벨에서 설정하는 속성으로, 해당 JVM 인스턴스 안에서만 유효하다.
- 설정 방법: `java -Dkey=value -jar app.jar`

---

## 3. 커맨드 라인 인수 - 단순 문자열

`main(String[] args)`의 인자로 넘어오는 값을 그대로 String으로 받아서 사용한다.

```java
// 실행 시: java -jar abc.jar dataA dataB
public static void main(String[] args) {
    for (String arg : args) {
        log.info("arg {}", arg);
    }
}
```

- 가장 단순한 방식으로, key-value 구분 없이 모든 인자가 단순 문자열로 들어온다.

---

## 4. 커맨드 라인 옵션 인수 - key=value 파싱

스프링의 `ApplicationArguments`를 활용하여 `--key=value` 형태의 옵션 인수를 파싱한다.

```java
// 실행 시: java -jar abc.jar --url=devdb --username=dev_user mode=on
ApplicationArguments appArgs = new DefaultApplicationArguments(args);

log.info("source Args = {}", List.of(appArgs.getSourceArgs()));       // 모든 인수
log.info("getNonOptionArgs = {}", appArgs.getNonOptionArgs());         // --가 없는 인수 (mode=on)
log.info("OptionsNames = {}", appArgs.getOptionNames());               // --가 붙은 옵션 이름들

Set<String> optionNames = appArgs.getOptionNames();
for (String optionName : optionNames) {
    log.info("Option arg {} = {}", optionName, appArgs.getOptionValues(optionName));
}
```

- `--`를 붙이면 스프링이 key-value로 인식한다. (`--url=devdb` → key: `url`, value: `devdb`)
- `--`가 없으면 Non-Option 인수로 분류된다. (`mode=on` → NonOptionArg)
- `getOptionValues()`의 반환 타입이 `List<String>`인 이유: `--url=devdb --url=devdb2`처럼 동일 키에 여러 값을 전달할 수 있기 때문이다.

---

## 5. ApplicationArguments 빈 주입

스프링 부트는 `ApplicationArguments`를 자동으로 빈으로 등록해둔다. 따라서 어떤 컴포넌트에서든 주입받아 사용할 수 있다.

```java
@Component
public class CommandLineBean {

    private final ApplicationArguments arguments;

    // 스프링이 ApplicationArguments를 빈으로 이미 등록해두었기 때문에 주입 가능
    public CommandLineBean(ApplicationArguments arguments) {
        this.arguments = arguments;
    }

    @PostConstruct
    public void init() {
        log.info("source {} ", List.of(arguments.getSourceArgs()));
        log.info("optionNames  {}", arguments.getOptionNames());
        Set<String> optionNames = arguments.getOptionNames();
        for (String optionName : optionNames) {
            log.info("option args {}={}", optionName, arguments.getOptionValues(optionName));
        }
    }
}
```

- `@PostConstruct`에서 사용하므로 빈 생성 직후 커맨드 라인 인수 정보를 확인할 수 있다.

---

## 6. Environment를 통한 통합 조회

OS 환경변수, 자바 시스템 속성, 커맨드 라인 옵션 인수는 **각각 사용하는 방법이 모두 다르다.** 스프링은 이 문제를 `Environment`와 `PropertySource`라는 추상화를 통해 해결한다.

```java
@Component
public class EnvironmentCheck {

    private final Environment env;

    public EnvironmentCheck(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        String url = env.getProperty("url");
        String username = env.getProperty("username");
        String password = env.getProperty("password");
    }
}
```

- 커맨드 라인 옵션 인수, 자바 시스템 속성, OS 환경변수 **모두** `env.getProperty()`라는 동일한 방법으로 조회할 수 있다.
- 어떤 소스에서 왔는지 신경 쓸 필요 없이 통합적으로 접근할 수 있다.

---

## 7. 외부 설정 파일

설정값이 많아지면 커맨드 라인 옵션이나 시스템 속성으로 관리하기 어렵다. 이때 외부 파일을 통해 설정 데이터를 가져올 수 있다.

- jar 파일이 위치한 디렉토리에 `application.properties` 파일을 두면, `Environment`가 해당 설정값을 읽어온다.
- `resources/application.properties`(내부)가 아닌, **jar 파일과 같은 경로에 위치한 외부 파일**을 의미한다.

### 외부 설정 파일의 한계

- 서버마다 설정 파일을 각각 관리해야 한다.
- 서버가 10대면 변경사항이 있을 때 10대 모두 수정해야 한다.
- 설정 변경 이력 관리도 어렵다.

---

## 8. 내부 설정 파일 - 프로필

외부 파일 관리의 번거로움을 해결하기 위해 **프로젝트 내부에 설정 파일을 포함**해서 관리한다.

### 파일 분리 방식

- `application-dev.properties` : 개발 환경 설정
- `application-prd.properties` : 운영 환경 설정
- 실행 시 `--spring.profiles.active=dev`로 프로필을 지정하면 해당 파일을 읽는다.

### 파일 합체 방식 - 단일 파일

`application.properties` 하나에 `#---` 구분자를 사용하여 논리적으로 문서를 분리할 수 있다.

```properties
# 기본값 (프로필 미지정 시 사용, local 환경)
url=local.db.com
username=local_user
password=local_pw

#---
spring.config.activate.on-profile=dev
url=dev.db.com
username=dev_user
password=dev_pw

#---
spring.config.activate.on-profile=prd
url=prod.db.com
username=prod_user
password=prod_pw
```

- 구분자: properties는 `#---`, yml은 `---`
- 프로필을 지정하지 않으면 `default` 프로필로 실행되므로, 기본값(local 환경)을 최상단에 설정해둔다.
- 스프링은 properties 파일을 **위에서 아래로 읽으며**, 프로필에 해당하는 값이 있으면 기본값을 **덮어쓴다**.

---

## 9. 우선순위

### 외부 설정 전체 우선순위 (아래로 갈수록 우선순위 높음)

| 순위 | 설정 소스 | 예시 |
|------|-----------|------|
| 1 | 설정 데이터 (application.properties) | `url=local.db.com` |
| 2 | OS 환경변수 | `export URL=devdb.com` |
| 3 | 자바 시스템 속성 | `java -Durl=devdb.com -jar app.jar` |
| 4 | 커맨드 라인 옵션 인수 | `java -jar app.jar --url=devdb.com` |
| 5 | `@TestPropertySource` | 테스트에서 사용 |

> 핵심 원칙: **변경하기 어려운 것보다 쉬운 것이, 범위가 넓은 것보다 좁은 것이 우선권을 가진다.**

### 설정 데이터 내부 우선순위 (아래로 갈수록 우선순위 높음)

| 순위 | 설정 데이터 위치 |
|------|-----------------|
| 1 | jar 내부 `application.properties` |
| 2 | jar 내부 프로필 적용 파일 `application-{profile}.properties` |
| 3 | jar 외부 `application.properties` |
| 4 | jar 외부 프로필 적용 파일 `application-{profile}.properties` |

> 같은 위치라면 프로필이 적용된 파일이 더 높은 우선순위를 가진다.
> 내부보다 외부 파일이 더 높은 우선순위를 가진다.

---

## 전체 흐름 정리

```
[외부 설정값을 읽는 방법]

OS 환경변수 (System.getenv)
  │  전역적, 모든 프로세스에서 사용
자바 시스템 속성 (-Dkey=value)
  │  JVM 레벨, 해당 인스턴스에서만 사용
커맨드 라인 인수 (args)
  │  단순 문자열 → --key=value로 옵션 인수 지원
  │  스프링이 ApplicationArguments를 빈으로 자동 등록
  ▼
문제: 각각 사용법이 다르다
  ▼
Environment로 통합 (env.getProperty("key"))
  │  어떤 소스에서 왔는지 신경 쓸 필요 없음
  ▼

[설정 파일 관리]

외부 설정 파일 (jar 옆에 application.properties)
  │  문제: 서버마다 파일을 따로 관리해야 함
  ▼
내부 설정 파일 + 프로필
  │  application-dev.properties / application-prd.properties
  │  또는 #--- 구분자로 단일 파일에 합체
  ▼
우선순위: 설정 데이터 < OS 환경변수 < 자바 시스템 속성 < 커맨드 라인 < @TestPropertySource
```

---

## 실무에서 권장되는 방식

### 기본 운영

내부 설정 파일(`application.properties`) 하나에 `#---` 구분자로 프로필별 설정을 관리하는 것이 일반적이다. 파일이 분리되어 있으면 한눈에 보기 어렵고, 단일 파일로 관리하면 환경별 차이를 바로 비교할 수 있다.

### 운영 중 특정 값을 변경해야 할 때

내부 설정 파일을 수정하면 jar를 다시 빌드해야 하므로, 우선순위를 활용하여 외부에서 값을 덮어쓴다.

| 방법 | 예시 | 적합한 상황 |
|------|------|------------|
| 커맨드 라인 옵션 인수 | `--url=newdb.com` | 값이 1~2개, 일시적 변경 |
| jar 외부 properties 파일 | jar 위치에 `application-prd.properties` 생성 | 변경할 값이 여러 개 |

- 우선순위 상 `jar 내부 < jar 외부 < 자바 시스템 속성 < 커맨드 라인`이므로 자연스럽게 오버라이드된다.
- jar 외부 파일에는 **변경이 필요한 key=value만 작성**하면 된다. 나머지는 내부 설정값이 그대로 적용된다.

### 주의: 재시작 필요

위 방식들은 모두 **애플리케이션 재시작이 필요**하다. properties 파일을 수정하거나 인수를 바꿔도 실행 중인 JVM에는 반영되지 않는다. 재시작 없이 설정을 반영하려면 Spring Cloud Config + `@RefreshScope` 같은 별도의 구성이 필요하다.
