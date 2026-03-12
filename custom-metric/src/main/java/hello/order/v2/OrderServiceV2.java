package hello.order.v2;

import hello.order.OrderService;
import io.micrometer.core.annotation.Counted;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OrderServiceV2 implements OrderService {

    /**
     * 핵심 비지니스 로직에 침투하는 걸 방지하기 위해
     * 마이크로미터에서 AOP로 만들어놨음
     * method 이름과 클래스 이름도 자동으로 들어간다.
     * 그 외에 result랑 exception도 추가됨
     */
    //private final MeterRegistry registry;

    private AtomicInteger stock = new AtomicInteger(100);


    @Counted("my.order")
    @Override
    public void order() {
        log.info("주문");
        stock.decrementAndGet();

    }

    @Counted("my.order")
    @Override
    public void cancel() {
        log.info("취소");
        stock.incrementAndGet();

    }

    @Override
    public AtomicInteger getStock() {
        return stock;
    }
}
