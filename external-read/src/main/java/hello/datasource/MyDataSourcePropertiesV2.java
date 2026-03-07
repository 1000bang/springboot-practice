package hello.datasource;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Getter
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV2 {

    /*
    이러한 환경설정등은 setter를 통해 값을 변경하면 너무 위험하다
    그래서 생성자를 통해 설정 정보를 주입하는  방식이 안전하다.

    @DefaultValue를 통해 해당 값을 찾을 수 없는 경우 기본값을 사용할 수 있음


    --
    참고 @ConstructorBinding
    스프링 부트 3.0 이전에는 생성자 바인딩 시에 @ConstructorBinding 애노테이션을 필수로 사용해야 했다.
    스프링 부트 3.0 부터는 생성자가 하나일 때는 생략할 수 있다.
    생성자가 둘 이상인 경우에는 사용할 생성자에 @ConstructorBinding 애노테이션을 적용하면 된다.


    문제
    타입과 객체를 통해 숫자에 문자가 들어오는 기본적인 문제는 해결되었지만
    max-connection의 값을 0으로 설정하면 커넥션이 하나도 만들어지지 않는 문제가 발생함
    최소값을 설정해서 애플리케이션 로딩시점에 예외를 발생시키려면 어떻게 할까
     */
    private String url;
    private String username;
    private String password;
    private Etc etc;

    public MyDataSourcePropertiesV2(String url, String username, String password, @DefaultValue Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    @Getter
    public static class Etc{
        private int maxConnection;
        private Duration timeout;
        private List<String> options;

        public Etc(int maxConnection, Duration timeout, @DefaultValue("DEFAULT") List<String> options) {
            this.maxConnection = maxConnection;
            this.timeout = timeout;
            this.options = options;
        }
    }

}
