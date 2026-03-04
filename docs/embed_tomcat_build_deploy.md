# 내장 톰캣과 빌드/배포

WAR 배포 방식의 한계를 이해하고, 내장 톰캣을 활용한 서블릿 등록, 스프링 연동, 그리고 빌드/배포 방식의 발전 과정을 정리한다.

---

## 1. WAR 배포 방식의 단점

기존 WAR 배포 방식은 다음과 같은 과정을 거쳐야 한다.

```
코드 작성 → WAR 빌드 → 톰캣 서버에 WAR 배포 → 톰캣 실행
```

### 문제점

| 문제 | 설명 |
|------|------|
| **톰캣을 별도로 설치해야 한다** | 개발 환경, 운영 환경 모두 톰캣을 따로 설치하고 관리해야 한다. |
| **톰캣 버전 관리가 어렵다** | 서버마다 톰캣 버전이 다를 수 있고, 버전 불일치로 인한 문제가 발생할 수 있다. |
| **배포 과정이 복잡하다** | WAR 파일을 빌드한 뒤, 톰캣의 `webapps` 디렉토리에 복사하고 톰캣을 재시작해야 한다. |
| **개발 환경 설정이 번거롭다** | IDE에서 톰캣 연동 설정이 필요하고, 로컬에서도 톰캣을 따로 실행해야 한다. |
| **단순 실행이 불가능하다** | `main()` 메서드로 바로 실행할 수 없다. 반드시 서블릿 컨테이너가 있어야 한다. |

> 핵심: **톰캣도 자바로 만들어진 프로그램이다.** 톰캣을 라이브러리로 포함시키고 `main()` 메서드에서 직접 실행하면 이 문제들을 해결할 수 있다. 이것이 **내장 톰캣(Embedded Tomcat)** 방식이다.

---

## 2. 내장 톰캣 설정

### 의존성 추가

```gradle
dependencies {
    implementation 'org.springframework:spring-webmvc:6.0.4'
    implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.1.5'
}
```

`tomcat-embed-core` 라이브러리를 추가하면 톰캣을 별도 설치 없이 자바 코드에서 직접 생성하고 실행할 수 있다.

### 내장 톰캣 기본 구조

모든 내장 톰캣 설정은 다음 세 단계를 따른다.

```java
// 1. 톰캣 생성 및 포트 설정
Tomcat tomcat = new Tomcat();
Connector connector = new Connector();
connector.setPort(8080);
tomcat.setConnector(connector);

// 2. 서블릿 또는 디스패처 서블릿 등록
Context context = tomcat.addContext("", "/");
tomcat.addServlet("", "servletName", servlet);
context.addServletMappingDecoded("/", "servletName");

// 3. 톰캣 시작
tomcat.start();
```

---

## 3. 내장 톰캣 + 서블릿

### EmbedTomcatServletMain - 순수 서블릿 등록

```java
public class EmbedTomcatServletMain {
    public static void main(String[] args) throws LifecycleException {
        // 내장 톰캣 설정
        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        // 서블릿 등록
        Context context = tomcat.addContext("", "/");
        tomcat.addServlet("", "helloServlet", new HelloServlet());
        context.addServletMappingDecoded("/", "helloServlet");
        tomcat.start();
    }
}
```

- `main()` 메서드에서 톰캣을 직접 생성하고 `HelloServlet`을 등록한다.
- 외부 톰캣 없이 `main()` 실행만으로 서버가 시작된다.
- `HelloServlet`은 `/` 경로로 들어오는 요청을 처리한다.

### HelloServlet

```java
public class HelloServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("HelloServlet.service");
        resp.getWriter().println("hello servlet!");
    }
}
```

---

## 4. 내장 톰캣 + 스프링

### EmbedTomcatSpringMain - 스프링 컨테이너 연동

```java
public class EmbedTomcatSpringMain {
    public static void main(String[] args) throws LifecycleException {
        // 1. 내장 톰캣 설정
        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        // 2. 스프링 컨테이너 생성
        AnnotationConfigWebApplicationContext appContext =
            new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // 3. 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 4. 디스패처 서블릿을 내장 톰캣에 등록
        Context context = tomcat.addContext("", "/");
        tomcat.addServlet("", "dispatcher", dispatcher);
        context.addServletMappingDecoded("/", "dispatcher");

        tomcat.start();
    }
}
```

**동작 흐름:**

```
main() 실행
  └─ 내장 톰캣 생성 (포트 8080)
  └─ 스프링 컨테이너 생성 (HelloConfig 등록)
  └─ DispatcherServlet 생성 ← 스프링 컨테이너 연결
  └─ 디스패처 서블릿을 톰캣에 등록
  └─ 톰캣 시작
```

- 스프링 컨테이너(`AnnotationConfigWebApplicationContext`)를 직접 생성하고 설정 클래스를 등록한다.
- `DispatcherServlet`에 스프링 컨테이너를 주입하여 연결한다.
- `/hello-spring` 요청 → `DispatcherServlet` → `HelloController.hello()` → "hello spring!" 반환

---

## 5. 빌드와 배포

### 일반 Jar 빌드

```gradle
task buildJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    with jar
}
```

```bash
./gradlew clean buildJar
```

**문제:** 일반 Jar는 내가 작성한 코드만 포함한다. **라이브러리(톰캣, 스프링 등)가 포함되지 않는다.**

- Jar 파일 안에 또 다른 Jar 파일을 포함할 수 없는 것이 자바 표준 스펙이다.
- 실행하면 `ClassNotFoundException`이 발생한다.
- 해결하려면 라이브러리 Jar 파일들을 별도 폴더에 두고 classpath로 지정해야 한다. → 배포가 복잡해진다.

### Fat Jar (Uber Jar) 빌드

```gradle
task buildFatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```

```bash
./gradlew clean buildFatJar
java -jar build/libs/embed-0.0.1-SNAPSHOT.jar
```

**해결 방식:** 라이브러리의 Jar 파일들을 모두 압축 해제하여 `.class` 파일 형태로 합쳐서 하나의 Jar에 담는다.

```
embed-0.0.1-SNAPSHOT.jar
  ├─ hello/embed/EmbedTomcatSpringMain.class  (내 코드)
  ├─ hello/spring/HelloController.class        (내 코드)
  ├─ org/springframework/...                   (스프링 라이브러리 class들)
  ├─ org/apache/catalina/...                   (톰캣 라이브러리 class들)
  └─ META-INF/MANIFEST.MF
```

### Fat Jar의 문제점

| 문제 | 설명 |
|------|------|
| **어떤 라이브러리가 포함되었는지 확인 어렵다** | 모든 Jar가 풀려서 class 파일로 합쳐지므로 원래 어떤 라이브러리에서 온 것인지 구분할 수 없다. |
| **파일명 중복 문제** | 서로 다른 라이브러리에 같은 경로의 파일이 존재할 수 있다. (`META-INF/services` 등) `DuplicatesStrategy.WARN`으로 경고만 출력하지만 하나의 파일이 무시된다. |

---

## 6. 스프링 부트 방식으로 개선

### MySpringApplication - 내장 톰캣 설정 자동화

```java
public class MySpringApplication {
    public static void run(Class configClass, String[] args) {
        // 내장 톰캣 설정
        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        // 스프링 컨테이너 생성
        AnnotationConfigWebApplicationContext appContext =
            new AnnotationConfigWebApplicationContext();
        appContext.register(configClass);

        // 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 디스패처 서블릿 등록
        Context context = tomcat.addContext("", "/");
        tomcat.addServlet("", "dispatcher", dispatcher);
        context.addServletMappingDecoded("/", "dispatcher");

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- 내장 톰캣 생성 + 스프링 컨테이너 생성 + 디스패처 서블릿 연결을 하나의 `run()` 메서드로 캡슐화했다.
- 설정 클래스(`configClass`)만 넘기면 모든 설정이 자동으로 이루어진다.

### MySpringBootApplication - 컴포넌트 스캔 어노테이션

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan
public @interface MySpringBootApplication {
}
```

- `@ComponentScan`을 포함하는 커스텀 어노테이션이다.
- 이 어노테이션이 붙은 클래스의 패키지를 기준으로 하위 패키지를 모두 스캔한다.
- 기존 `HelloConfig`의 `@Configuration`이 필요 없어진다. 컴포넌트 스캔이 `@RestController`가 붙은 `HelloController`를 자동으로 찾아 등록하기 때문이다.

### MySpringBootMain - 최종 실행

```java
@MySpringBootApplication
public class MySpringBootMain {
    public static void main(String[] args) {
        MySpringApplication.run(MySpringBootMain.class, args);
    }
}
```

- `main()` 메서드 한 줄로 내장 톰캣 + 스프링 컨테이너 + 디스패처 서블릿이 모두 설정된다.
- 실제 스프링 부트의 `SpringApplication.run()` 과 동일한 구조이다.

---

## 7. 스프링 부트의 실행 가능 Jar - Fat Jar 문제 해결

`MySpringBootMain`은 **내장 톰캣 설정의 복잡함**을 해결한 것이지, Fat Jar 문제를 해결한 것이 아니다. Fat Jar 문제는 스프링 부트가 제공하는 **실행 가능 Jar(Executable Jar)** 라는 별도의 구조로 해결한다.

### Fat Jar vs 스프링 부트 실행 가능 Jar

```
# Fat Jar - 라이브러리 Jar를 풀어서 class로 합침
my-app.jar
  ├─ hello/MyMain.class
  ├─ org/springframework/xxx.class       ← 어떤 라이브러리인지 구분 불가
  ├─ org/apache/catalina/xxx.class       ← 파일명 중복 시 하나가 사라짐
  └─ META-INF/MANIFEST.MF

# 스프링 부트 실행 가능 Jar - 라이브러리 Jar를 그대로 보관
my-app.jar
  ├─ BOOT-INF/classes/                   ← 내 코드
  │    └─ hello/MyMain.class
  ├─ BOOT-INF/lib/                       ← 라이브러리 Jar를 그대로 보관
  │    ├─ spring-webmvc-6.0.4.jar        ← 어떤 라이브러리인지 바로 보임
  │    └─ tomcat-embed-core-10.1.5.jar   ← 파일명 중복 문제 없음
  └─ org/springframework/boot/loader/
       └─ JarLauncher.class              ← 중첩 Jar를 읽는 커스텀 클래스로더
```

| | Fat Jar | 스프링 부트 실행 가능 Jar |
|---|---------|------------------------|
| **방식** | 라이브러리 Jar를 풀어서 class로 합침 | 라이브러리 Jar를 그대로 `BOOT-INF/lib/`에 보관 |
| **라이브러리 구분** | 불가능 (class가 섞임) | 가능 (Jar 파일명 그대로 유지) |
| **파일명 중복** | 하나가 무시됨 | 각 Jar가 독립적이므로 문제 없음 |
| **실행 방법** | `java -jar` | `java -jar` (JarLauncher가 중첩 Jar 로딩) |

> 자바 표준으로는 Jar 안의 Jar를 읽을 수 없다. 스프링 부트는 `JarLauncher`라는 커스텀 클래스로더를 통해 이 제한을 자체적으로 우회했다.

---

## 8. 전체 발전 과정 정리

스프링 부트가 해결하는 문제는 크게 두 가지로 나뉜다.

```
[문제 1: 서버 설정의 복잡함]

WAR 배포 방식 (외부 톰캣 필요)
  │  문제: 톰캣 설치/관리, 복잡한 배포 과정
  ▼
내장 톰캣 + 서블릿 (EmbedTomcatServletMain)
  │  main()으로 직접 실행 가능, 하지만 스프링 없음
  ▼
내장 톰캣 + 스프링 (EmbedTomcatSpringMain)
  │  스프링 컨테이너 + 디스패처 서블릿 수동 연결
  │  문제: 매번 톰캣 설정 코드를 반복해야 함
  ▼
설정 자동화 (MySpringApplication.run)
  │  톰캣 + 스프링 설정을 하나의 메서드로 캡슐화
  ▼
커스텀 어노테이션 (@MySpringBootApplication)
  │  @ComponentScan으로 수동 빈 등록 불필요
  ▼
최종 (MySpringBootMain)
     main() 한 줄로 모든 설정 완료
     ≈ 스프링 부트의 SpringApplication.run()


[문제 2: 빌드/배포의 복잡함]

일반 Jar
  │  문제: 라이브러리가 포함되지 않음
  ▼
Fat Jar
  │  문제: 라이브러리 구분 불가, 파일명 중복
  ▼
스프링 부트 실행 가능 Jar
     Jar 안에 Jar를 그대로 보관 + JarLauncher로 로딩
```

> 스프링 부트는 이 두 가지를 모두 해결한다. `SpringApplication.run()`으로 내장 톰캣 설정을 자동화하고, 실행 가능 Jar 구조로 Fat Jar의 문제도 해결한다.
