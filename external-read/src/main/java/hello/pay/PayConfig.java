package hello.pay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class PayConfig {

    /*
    결제 환경을 세팅할 때 로컬환경에서 실제 결제가 등록되면 안된다.
    따라서 상황에 따라서 LocalPayClient 또는 ProdPayClient 를 주입받는다.
     */
    @Bean
    @Profile("default")
    public LocalPayClient localPayClient() {
        log.info("localPayClient 빈 등록");
        return new LocalPayClient();
    }

    @Bean
    @Profile("prd")
    public ProdPayClient prodPayClient() {
        log.info("prodPayClient 빈 등록");
        return new ProdPayClient();
    }
}
