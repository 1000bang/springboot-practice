package hello.datasource;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


//외부 설정을 주입 받을 객체를 생성한다.
// 그리고 각 필드를 외부 설정의 키 값에 맞추어 준비한다.
@Data
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV1 {

    private String url;
    private String username;
    private String password;
    private Etc etc;

    @Data
    public static class Etc{
        private int maxConnection;
        private Duration timeout;
        private List<String> options = new ArrayList<>();
    }

}
