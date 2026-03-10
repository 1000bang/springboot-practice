loggers 엔드포인트를 사용하면 로깅과 관련된 정보를 확인하고, 또 실시간으로 변경할 수도 있다. 코드를 통해서 알 아보자.

```java
@Slf4j @RestController public class LogController {
@GetMapping("/log") public String log() {
    log.trace("trace log");
    log.debug("debug log");
    log.info("info log");
    log.warn("warn log");
    log.error("error log");
    return "ok";
    }
}
```
로그를 별도로 설정하지 않으면 스프링 부트는 기본으로 INFO 를 사용한다. 실행 결과를 보면 ROOT 의 configuredLevel 가 INFO 인 것을 확인할 수 있다. 따라서 그 하위도 모두 INFO 레벨이 적용된다.

application.yml 설정 
```yaml
logging:
    level:
        hello.controller: debug
```


hello.controller 패키지와 그 하위는 debug 레벨을 출력하도록 했다. 이제 앞서 만든 LogController 
클래스도 debug 레벨의 영향을 받는다.


loggers 엔드포인트를 호출해보자.
실행
http://localhost:8080/actuator/loggers

```json
{
  "levels": [
    "OFF",
    "ERROR",
    "WARN",
    "INFO",
    "DEBUG",
    "TRACE"
  ],
  "loggers": {
    "ROOT": {
      "configuredLevel": "INFO",
      "effectiveLevel": "INFO"
    },
    "hello": {
      "effectiveLevel": "INFO"
    },
    "hello.MicroMeterApplication": {
      "effectiveLevel": "INFO"
    },
    "hello.controller": {
      "configuredLevel": "DEBUG",
      "effectiveLevel": "DEBUG"
    },
    "hello.controller.LogController": {
      "effectiveLevel": "DEBUG"
    }
  }
}
```


실시간 로그레벨 변경
개발 서버는 보통 DEBUG 로그를 사용하지만, 운영 서버는 보통 요청이 아주 많다. 따라서 로그도 너무 많이 남기 때문 에 DEBUG 로그까지 모두 출력하게 되면 성능이나 디스크에 영향을 주게 된다. 그래서 운영 서버는 중요하다고 판단되 는 INFO 로그 레벨을 사용한다.
그런데 서비스 운영중에 문제가 있어서 급하게 DEBUG 나 TRACE 로그를 남겨서 확인해야 확인하고 싶다면 어떻게 해 야할까? 일반적으로는 로깅 설정을 변경하고, 서버를 다시 시작해야 한다.

loggers 엔드포인트를 사용하면 애플리케이션을 다시 시작하지 않고, 실시간으로 로그 레벨을 변경할 수 있다.
다음을 Postman 같은 프로그램으로 POST로 요청해보자(꼭! POST를 사용해야 한다.) 
POST http://localhost:8080/actuator/loggers/hello.controller 
POST로 전달하는 내용 JSON , content/type 도 application/json 으로 전달해야 한다.

```json
{

"configuredLevel": "TRACE"

}
```

요청에 성공하면 204 응답이 온다.
GET으로 요청해서 확인해보면 configuredLevel 이 TRACE 로 변경된 것을 확인할 수 있다.
GET http://localhost:8080/actuator/loggers/hello.controller