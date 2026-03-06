package hello;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnvironmentCheck {

    /*
        우리는 지금까지 커맨드 라인 옵션, 자바 시스템 속성, OS 환경변수를 알아봤다.
        하지만 위 애들은 각각 사용하는 방법이 다 달랐다.

        이를 스프링이 이 문제를 Environment 와 PropertySource 라는 추상화를 통해서 해결한다.

     */
    private final Environment env;

    public EnvironmentCheck(Environment env) {
        this.env = env;
    }


    //  커맨드 라인 옵션인수, 자바 시스템 속성 모두 Enviroment를 통해 동일한 방법으로 읽을 수 있는 것을 확인
    @PostConstruct
    public void init () {
        String url = env.getProperty("url");
        String username = env.getProperty("username");
        String password = env.getProperty("password");
        log.info("env url = {}", url);
        log.info("env username = {}", username);
        log.info("env password = {}", password);
    }
    // 시스템 속성, 커맨드 라인 둘다 설정하면 어케 될까
    // -> 커맨드 라인이 더 우선순위를 가진다.
    // 변경하기 어려운 파일보다 실행 시 원하는 값을 줄 수 있는 자바 시스템 속성이 더 우선권을 가진다.
    // 범위가 넓은것 보다 좁은 것이 우선권을 가진다.


    // 너무 길면 옵션인수와 시스템속성으로 처리하는게 힘들고 관리가 어렵다.
    // 이 때 외부 파일을 통해 설정 데이터를 가져올 수 있다.
    //jar 파일이 위치한 디렉토리에 applicaton.properties 와 application.yml 등의 파일이 있으면
    // (resources/application.properites 아님 )
    //env가 해당 설정값을 가져온다.

    // 남은 문제
    // 외부 설정을 별도의 파일로 관리하면 서버마다 설정 파일 자체를 관리해야 해서 번거롭다..
    // 서버가 10대면 변경사항이 있을 때 10대 서버 모두 각각 변경해야함
    // 설정값 변경 이력도 어려움

    //설정 파일을 외부에 관리하는 것이 상당히 번거롭다.
    // 그래서 해결방법은 설정파일을 프로젝트 내부에 포함해서 관리하는 것이다.
    // 개발용 application-dev , application-prd
    // 실행할 때 최소한의 값을 넘겨줌 dev, prd
    // dev프로필이 넘어오면 application-dev를 읽어서 사용하게
    //--spring.active.profile=dev 로 설정하면됨


    // 파일을 분리해서 관리하면 한눈에 보기 어렵다
    // 내부 파일 합체 - application.properties에서 합쳐봄
    // spring.config.activate.on-profile=dev 프로필 값 지정
    // #--- !--- 를 통해 논리적인 문서 구분 yml 은 ---

    //이 때 profile을 지정하지 않고 실행하면 default로 실행됨
    // 그래서 로컬 환경에서 기본 값을 많이 설정해둠
    // 스프링은 Properties파일을 위에서 아래로 읽는다. 그래서
    // 처음 기본값을 세팅하고 프로필에 해당하는게 있으면
    // 그 값을 덮어쓴다.
    // 그래서 기본값은 파일 최상단에 써야한다.


    // 우선순위 (낮은 순) 아래로 갈 수록 우선 순위가 높다.
    // 1. 설정 데이터 application.properties
    // 2. OS환경 변수
    // 3. 자바 시스템 속성 vm options -Dspring=a
    // 4. 커맨드 라인 옵션 인수 --option=a
    // 5. @TestPropertySource 테스트에서 사용

    //설정 데이터 우선순위 (낮은 순) 아래로 갈 수록 우선 순위가 높다.
    // 1. jar 내부 application.properties
    // 2. jar 내부 프로필 적용 파일 application-{profile}.properties
    // 3. jar 외부 application.properties
    // 4. jar 외부 프로필 적용 파일 application-{profile}.properties

}

