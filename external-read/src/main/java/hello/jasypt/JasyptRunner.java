package hello.jasypt;

import hello.pay.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JasyptRunner implements ApplicationRunner {

    @Value("${test.jasypt.value}")
    String jasyptValue;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(jasyptValue);
    }
}
