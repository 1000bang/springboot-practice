package hello.datasource;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;


@Getter
@ConfigurationProperties("my.datasource")
@Validated
public class MyDataSourcePropertiesV3 {

    /*
    커넥션 숫자를 최소 1 최대 999 범위를 가져야 한다면 어떻게 검증할 수 있을까
    이메일을 외부설정에 입력했는데 이메일 형식에 맞지 않는다면 ?
    자바 빈 검증기라는 휼륭한 표준 검증기가 제공된다.

    패키지 이름에 jakarta.validation 으로 시작하는 것은 자바 표준 검증기에서 지원하는 기능이다.
    org.hibernate.validator 로 시작하는 것은 자바 표준 검증기에서 아직 표준화 된 기능은 아니고,
    하이버네이트 검증기라는 표준 검증기의 구현체에서 직접 제공하는 기능이다


    Property: my.datasource.etc.maxConnection
    Value: "0"
    Origin: class path resource [application_backup.properties] - 5:34
    Reason: 1 이상이어야 합니다
     */
    @NotEmpty
    private String url;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @Valid //스프링 부트 3.4 이상의 경우 @Valid가 있어야 중첩 필드에 유효성 검사가 적용된다.
    private Etc etc;

    public MyDataSourcePropertiesV3(String url, String username, String password, @DefaultValue Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    @Getter
    public static class Etc{
        @Min(1) @Max(999)
        private int maxConnection;

        @DurationMin(seconds = 1)
        @DurationMax(seconds = 60)
        private Duration timeout;

        private List<String> options;

        public Etc(int maxConnection, Duration timeout, @DefaultValue("DEFAULT") List<String> options) {
            this.maxConnection = maxConnection;
            this.timeout = timeout;
            this.options = options;
        }
    }

}
