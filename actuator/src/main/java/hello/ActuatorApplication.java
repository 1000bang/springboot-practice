package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ActuatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActuatorApplication.class, args);
    }

    @Bean
    public InMemoryHttpExchangeRepository httpExchangeRepository() {
        return new InMemoryHttpExchangeRepository();
    }
    //이 구현체는 최대 100개의 HTTP 요청을 제공한다. 최대 요청이 넘어가면 과거 요청을 삭제한다. setCapacity() 로 최대 요청수를 변경할 수 있다.

}

/*
엔드포인트 목록
beans : 스프링 컨테이너에 등록된 스프링 빈을 보여준다.
conditions : condition 을 통해서 빈을 등록할 때 평가 조건과 일치하거나 일치하지 않는 이유를 표시한다.
configprops : @ConfigurationProperties 를 보여준다.
env : Environment 정보를 보여준다.
health : 애플리케이션 헬스 정보를 보여준다.
httpexchanges : HTTP 호출 응답 정보를 보여준다. HttpExchangeRepository 를 구현한 빈을 별도로 등록해야 한다.
info : 애플리케이션 정보를 보여준다.
loggers : 애플리케이션 로거 설정을 보여주고 변경도 할 수 있다.
metrics : 애플리케이션의 메트릭 정보를 보여준다.
mappings : @RequestMapping 정보를 보여준다.
threaddump : 쓰레드 덤프를 실행해서 보여준다.
shutdown : 애플리케이션을 종료한다. 이 기능은 기본으로 비활성화 되어 있다.

전체 엔드포인트는 다음 공식 메뉴얼을 참고하자.
https://docs.spring.io/spring-boot/docs/current/reference/html/ actuator.html#actuator.endpoints
 */