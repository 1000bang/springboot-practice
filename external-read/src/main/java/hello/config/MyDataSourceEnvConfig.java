package hello.config;

import hello.datasource.MyDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.util.List;

@Slf4j
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
        List<String> property = env.getProperty("my.datasource.etc.options", List.class);

        return new MyDataSource(url, username, password, maxConnection, timeout, property);

    }

    /*
    application_backup.properties 에 필요한 외부 설정을 추가하고 Environment를 통해 해당 값들을 읽어서 MyDataSource 를 만들었다.
    향후 외부 설정 방식이 달라져도 애플리케이션 코드를 그대로 유지할 수 있다.

    하지만 이 방식의 단점은 Environment를 직접 주입받고, env.getProperty(key)를 통해 값을 꺼내는 과정을 반복해야 한다는 점이다.
    스프링은 @Value를 통해 외부 설정값을 주입받는 더 편리한 기능을 제공한다.
     */
}
