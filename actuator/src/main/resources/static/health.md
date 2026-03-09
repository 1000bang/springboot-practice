헬스 정보를 사용하면 애플리케이션에 문제가 발생했을 때 문제를 빠르게 인지할 수 있다.

http://localhost:8080/actuator/health

```aiignore
{"status" : "UP"}
```
헬스 정보는 단순히 애플리케이션이 요청에 응답을 할 수 있는지 판단하는 것을 넘어서 애플리케이션이 사용하는 데이 터베이스가 응답하는지, 디스크 사용량에는 문제가 없는지 같은 다양한 정보들을 포함해서 만들어진다.

헬스 정보를 더 자세히 보려면 다음 옵션을 지정하면 된다.
management.endpoint.health.show-details=always

```yaml
management:
    endpoint:
        health:
            show-details: always
```

```Json
{ 
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 994662584320,
        "free": 303418753024,
        "threshold": 10485760,
        "path": ".../spring-boot/actuator/actuator/.",
        "exists": true
      } },
    "ping": {
      "status": "UP"
    }
  }
}
```

각각의 항목이 아주 자세하게 노출되는 것을 확인할 수 있다.

이렇게 자세하게 노출하는 것이 부담스럽다면 show-details 옵션을 제거하고 대신에 다음 옵션을 사용하면 된다.

management.endpoint.health.show-components=always


헬스 이상 상태

헬스 컴포넌트 중에 하나라도 문제가 있으면 전체 상태는 DOWN 이 된다.


```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN"
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

참고 - 자세한 헬스 기본 지원 기능은 다음 공식 메뉴얼을 참고하자

https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.auto-configured-health-indicators

원하는 경우 직접 헬스 기능을 구현해서 추가할 수 있다. 직접 구현하는 일이 많지는 않기 때문에 필요한 경우 다 음 공식 메뉴얼을 참고하자
https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.writing-custom-health-indicators