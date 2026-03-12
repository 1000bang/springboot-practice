package hello.order.gauge;

import hello.order.OrderService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class StockConfigV2 {

    /*
    MeterBinder를 통해 간단하게 등록할 수 있다
     */
    @Bean
    public MeterBinder stockSize(OrderService orderService){
        return meterRegistry -> Gauge.builder("my.stock", orderService::getStock).register(meterRegistry);

    }
}
