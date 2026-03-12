package hello.order.v3;

import hello.order.OrderService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OrderServiceV3  implements OrderService {

    /*
    Timer는 시간을 측정하는데 사용된다.
    seconds_count : 누적 실행 수 : 카운터
    seconds_sum : 실행 시간의 합 : sum
    seconds_max : 최대 실행 시간 (가장 오래걸린) : 게이지 최근 몇분간
     */

    private final MeterRegistry registry;
    private AtomicInteger stock = new AtomicInteger(100);

    public OrderServiceV3(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void order() {
        Timer timer = Timer.builder("my.order")
                .tag("class", this.getClass().getName())
                .tag("method", "order")
                .description("order")
                .register(registry);
        timer.record(()->{
            log.info("주문");
            stock.decrementAndGet();
            sleep(500);
        });
    }


    @Override
    public void cancel() {

        Timer timer = Timer.builder("my.order")
                .tag("class", this.getClass().getName())
                .tag("method", "cancel")
                .description("cancel")
                .register(registry);
        timer.record(() -> {
           log.info("취소");
           stock.incrementAndGet();
           sleep(200);
        });
    }

    @Override
    public AtomicInteger getStock() {
        return stock;
    }

    private static void sleep(int l) {
        try {
            Thread.sleep(l + new Random().nextInt(200));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
