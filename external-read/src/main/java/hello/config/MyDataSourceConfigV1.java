package hello.config;

import hello.datasource.MyDataSource;
import hello.datasource.MyDataSourcePropertiesV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
//@EnableConfigurationProperties(MyDataSourcePropertiesV1.class)
//EnableConfigurationProperties 설정을 해야 @ConfigurationProperties 이걸 읽을 수 있음
//근데 컴포넌트 스캔처럼 자동으로 빈으로 등록해서 쓰면안되나..
//응 할 수 있음 @ConfigurationPropertiesScan
@ConfigurationPropertiesScan
public class MyDataSourceConfigV1 {

    private final MyDataSourcePropertiesV1 properties;

    public MyDataSourceConfigV1(MyDataSourcePropertiesV1 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource dataSource () {
        return new MyDataSource(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getEtc().getMaxConnection(),
                properties.getEtc().getTimeout(),
                properties.getEtc().getOptions()
        );
    }
    //ConfigurationProperties 를 사용하면 타입 안전한 설정 속성을 사용할 수 있다.
    //maxConnecton을 abc로 입력하고 실행하면 오류가 발생해 실수를 방지해준다.
}
