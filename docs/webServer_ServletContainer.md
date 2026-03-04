# 1. 서블릿 컨테이너 초기화와 스프링 MVC 연동

서블릿 컨테이너 초기화를 사용해서 필요한 서블릿도 등록하고, 스프링 컨테이너도 생성해서 등록하고 또 스프링 MVC가 동작하도록 디스패처 서블릿도 중간에 연결해보았다. 그리고 스프링이 제공하는 좀 더 편리한 초기화 방법도 알아보았다.

---

## 1. 서블릿 컨테이너 초기화 - ServletContainerInitializer

서블릿 컨테이너는 `ServletContainerInitializer` 인터페이스를 구현한 클래스를 자동으로 찾아 실행한다.
등록 방법은 `META-INF/services/jakarta.servlet.ServletContainerInitializer` 파일에 구현 클래스의 경로를 작성하는 것이다.

```
hello.container.MyContainerInitV1
hello.container.MyContainerInitV2
```

### MyContainerInitV1 - 단순 초기화 확인

```java
public class MyContainerInitV1 implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        System.out.println("MyContainerInitV1 onStartup");
        System.out.println("MyContainerInitV1 set = "+ set);
        System.out.println("MyContainerInitV1 servletContext = "+ servletContext);
    }
}
```

서블릿 컨테이너가 시작될 때 `onStartup()`이 호출되는 것을 확인하는 용도이다.

### MyContainerInitV2 - @HandlesTypes를 활용한 애플리케이션 초기화

```java
@HandlesTypes(AppInit.class)
public class MyContainerInitV2 implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        for (Class<?> appInitClass : set) {
            AppInit appInit = (AppInit) appInitClass.getDeclaredConstructor().newInstance();
            appInit.onStartup(servletContext);
        }
    }
}
```

- `@HandlesTypes(AppInit.class)`: 서블릿 컨테이너가 `AppInit` 인터페이스의 구현체들을 찾아서 `set` 파라미터로 넘겨준다.
- 넘겨받은 구현체들을 리플렉션으로 인스턴스화한 뒤 `onStartup()`을 호출하여 각각의 초기화 로직을 실행한다.
- 이 구조 덕분에 `AppInit` 구현체를 추가하기만 하면 자동으로 초기화에 포함된다.

---

## 2. 애플리케이션 초기화 인터페이스

```java
public interface AppInit {
    void onStartup(ServletContext servletContext);
}
```

`MyContainerInitV2`가 이 인터페이스의 구현체를 자동으로 찾아 실행해주므로, 개발자는 이 인터페이스만 구현하면 된다.

---

## 3. 단계별 서블릿/스프링 등록 방식

### V1 - 순수 서블릿 등록 (AppInitV1Servlet)

```java
public class AppInitV1Servlet implements AppInit {
    @Override
    public void onStartup(ServletContext servletContext) {
        ServletRegistration.Dynamic helloServlet =
            servletContext.addServlet("helloServlet", new HelloServlet());
        helloServlet.addMapping("/hello-servlet");
    }
}
```

- `ServletContext`를 사용하여 프로그래밍 방식으로 서블릿을 직접 등록한다.
- `HelloServlet`을 생성하고 `/hello-servlet` 경로에 매핑한다.
- 스프링 없이 순수 서블릿만 사용하는 방식이다.

### V2 - 스프링 컨테이너 생성 + 디스패처 서블릿 수동 연결 (AppInitV2Spring)

```java
public class AppInitV2Spring implements AppInit {
    @Override
    public void onStartup(ServletContext servletContext) {
        // 1. 스프링 컨테이너 생성
        AnnotationConfigWebApplicationContext appContext =
            new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // 2. 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
        // DispatcherServlet은 스프링 MVC의 핵심 서블릿으로, 서블릿 컨테이너로부터 요청을 받아 적절한 스프링 컨트롤러에게 라우팅하고 처리를 위임하는 게이트웨이 역할을 합니다.
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 3. 디스패처 서블릿을 서블릿 컨테이너에 등록
        ServletRegistration.Dynamic servlet =
            servletContext.addServlet("dispatcherV2", dispatcher);
        servlet.addMapping("/spring/*");
    }
}
```

**동작 흐름:**
1. `AnnotationConfigWebApplicationContext`로 스프링 컨테이너를 생성하고 `HelloConfig`를 등록한다.
2. `DispatcherServlet`을 생성하면서 스프링 컨테이너를 주입한다.
3. 생성한 디스패처 서블릿을 서블릿 컨테이너에 `dispatcherV2`라는 이름으로 등록하고, `/spring/*` 경로에 매핑한다.

이 방식은 스프링 컨테이너와 디스패처 서블릿을 직접 생성하고 연결하는 과정을 모두 수동으로 수행한다.

### V3 - 스프링이 제공하는 편리한 초기화 (AppInitV3SpringMvc)

```java
public class AppInitV3SrpingMvc implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // 1. 스프링 컨테이너 생성
        AnnotationConfigWebApplicationContext appContext =
            new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // 2. 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 3. 디스패처 서블릿을 서블릿 컨테이너에 등록
        ServletRegistration.Dynamic servlet =
            servletContext.addServlet("dispatcherV3", dispatcher);
        servlet.addMapping("/");
    }
}
```


- `AppInit` 대신 스프링이 제공하는 `WebApplicationInitializer` 인터페이스를 구현한다.
- 스프링은 내부적으로 `SpringServletContainerInitializer`를 통해 `@HandlesTypes(WebApplicationInitializer.class)`로 구현체를 자동 탐색한다.
- 따라서 별도의 `MyContainerInitV2` 같은 커스텀 초기화 클래스 없이도, `WebApplicationInitializer`만 구현하면 스프링이 알아서 초기화해준다.
- 매핑 경로가 `/`이므로 모든 요청을 디스패처 서블릿이 처리한다.
-  `ServletContainerInitializer`를 사용하여 `WebApplicationInitializer` 인터페이스 구현체를 찾아 실행합니다. 개발자는 이 인터페이스만 구현하면 초기화 로직을 작성할 수 있습니다
---

## 4. 스프링 MVC 구성

### HelloConfig - 스프링 설정

```java
@Configuration
public class HelloConfig {
    @Bean
    public HelloController helloController() {
        return new HelloController();
    }
}
```

### HelloController - 컨트롤러

```java
@RestController
public class HelloController {
    @GetMapping("/hello-spring")
    public String hello() {
        return "Hello World";
    }
}
```

`HelloConfig`가 `HelloController`를 빈으로 등록하고, `/hello-spring` GET 요청에 대해 "Hello World"를 반환한다.

---

## 5. 전체 흐름 정리

```
서블릿 컨테이너 시작
  └─ META-INF/services 파일에서 ServletContainerInitializer 구현체 탐색
      ├─ MyContainerInitV1.onStartup() → 초기화 로그 출력
      ├─ MyContainerInitV2.onStartup() → @HandlesTypes로 AppInit 구현체 탐색
      │    ├─ AppInitV1Servlet → HelloServlet을 /hello-servlet에 등록
      │    └─ AppInitV2Spring → 스프링 컨테이너 생성 + DispatcherServlet을 /spring/*에 등록
      └─ SpringServletContainerInitializer (스프링 내장)
           └─ AppInitV3SpringMvc → 스프링 컨테이너 생성 + DispatcherServlet을 /에 등록
```

| 방식 | 클래스 | 핵심 | URL 매핑 |
|------|--------|------|----------|
| V1 - 순수 서블릿 | `AppInitV1Servlet` | `servletContext.addServlet()` 으로 직접 등록 | `/hello-servlet` |
| V2 - 스프링 수동 연결 | `AppInitV2Spring` | 스프링 컨테이너 + 디스패처 서블릿을 직접 생성/연결 | `/spring/*` |
| V3 - 스프링 편리한 초기화 | `AppInitV3SrpingMvc` | `WebApplicationInitializer` 구현만으로 자동 초기화 | `/` |
