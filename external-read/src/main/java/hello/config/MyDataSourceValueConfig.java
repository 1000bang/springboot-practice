package hello.config;

import hello.datasource.MyDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
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

    //@Value 를 사용하면 외부 설정값을 편리하게 주입받을 수 있다. (@Value 도 내부에서는 Environment 를 사용한다.)
    // type변환도 자동으로 해줌
    // :을 사용해서 기본값을 지정해줄 수 있다. (해당 값이 없으면 기본값 사용)
    @Bean
    public MyDataSource myDataSource () {
        return new MyDataSource(url, username, password, maxConnection, timeout, options);
    }


    //@Value 는 필드에 사용할 수도 있고, 파라미터에 사용할 수도 있다.
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

    /*
    단점
    @Value 를 사용하는 방식도 좋지만, @Value 로 하나하나 외부 설정 정보의 키 값을 입력받고, 주입 받아와야 하는 부 분이 번거롭다.
     그리고 설정 데이터를 보면 하나하나 분리되어 있는 것이 아니라 정보의 묶음으로 되어 있다.
     여기서는 my.datasource 부분으로 묶여있다.
     이런 부분을 객체로 변환해서 사용할 수 있다면 더 편리하고 더 좋을 것이다.
     */
}
