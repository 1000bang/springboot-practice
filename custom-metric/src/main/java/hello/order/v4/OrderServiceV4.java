package hello.order.v4;

import hello.order.OrderService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Timed("my.order")
@Slf4j
public class OrderServiceV4 implements OrderService {

    /*
    Timer 도 Counter와 마찬가지로 어노테이션으로 사용가능하다.
    @Timed("my.order") 타입이나 메서드 중에 적용할 수 있다.
    퍼블릭 메서드에 적용된다.
     */
    private AtomicInteger stock = new AtomicInteger(100);


    @Override
    public void order() {
        log.info("주문");
        stock.decrementAndGet();
        sleep(500);
    }


    @Override
    public void cancel() {

        log.info("취소");
        stock.incrementAndGet();
        sleep(200);
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
