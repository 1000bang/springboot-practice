# 커스텀 메트릭

마이크로미터가 기본으로 제공하는 메트릭 외에, 비즈니스에 특화된 메트릭을 직접 만들어야 할 때가 있다. 예를 들어 주문 수, 취소 수, 재고 수량 같은 지표는 기본 메트릭에 포함되지 않으므로 직접 등록해야 한다. 마이크로미터는 Counter, Timer, Gauge 세 가지 핵심 메트릭 타입을 제공하며, 각각을 수동 등록하거나 AOP 애노테이션으로 간편하게 적용할 수 있다.

---

## 메트릭 타입 개요

| 타입 | 용도 | 특징 |
|------|------|------|
| **Counter** | 누적 횟수 측정 | 단조 증가만 가능 (주문 수, 요청 수) |
| **Timer** | 실행 시간 측정 | 호출 횟수 + 총 시간 + 최대 시간을 함께 기록 |
| **Gauge** | 현재 값 측정 | 증가/감소 모두 가능 (재고 수량, 활성 커넥션 수) |

---

## 1. Counter (카운터)

단조 증가하는 값을 측정한다. 값이 줄어들지 않고 계속 증가만 한다. 누적 주문 수, HTTP 요청 수 등에 사용한다.

### V1: MeterRegistry를 직접 사용

`MeterRegistry`를 주입받아 메서드 내부에서 직접 카운터를 등록하고 증가시키는 방식이다.

```java
@Service
public class OrderServiceV1 implements OrderService {

    private final MeterRegistry registry;
    private AtomicInteger stock = new AtomicInteger(100);

    public OrderServiceV1(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void order() {
        stock.decrementAndGet();
        Counter.builder("my.order")
                .tag("class", this.getClass().getName())
                .tag("method", "order")
                .description("order")
                .register(registry).increment();
    }

    @Override
    public void cancel() {
        stock.incrementAndGet();
        Counter.builder("my.order")
                .tag("class", this.getClass().getName())
                .tag("method", "cancel")
                .description("cancel")
                .register(registry).increment();
    }
}
```

- `Counter.builder("my.order")` : `my.order`라는 이름으로 카운터를 생성한다.
- `.tag("method", "order")` : 태그로 같은 메트릭 이름 안에서 주문과 취소를 구분한다.
- `.register(registry).increment()` : MeterRegistry에 등록하고 값을 1 증가시킨다.

**Config 클래스**

```java
@Configuration
public class OrderConfigV1 {
    @Bean
    OrderService orderService(MeterRegistry registry) {
        return new OrderServiceV1(registry);
    }
}
```

### V2: @Counted 애노테이션 (AOP)

AOP를 사용하면 비즈니스 로직에 메트릭 코드를 넣지 않아도 된다. `@Counted` 애노테이션만 붙이면 메서드 호출 시 자동으로 카운터가 증가한다.

```java
@Service
public class OrderServiceV2 implements OrderService {

    private AtomicInteger stock = new AtomicInteger(100);

    @Counted("my.order")
    @Override
    public void order() {
        stock.decrementAndGet();
    }

    @Counted("my.order")
    @Override
    public void cancel() {
        stock.incrementAndGet();
    }
}
```

```java
@Configuration
public class OrderConfigV2 {
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    @Bean
    OrderService orderService() {
        return new OrderServiceV2();
    }
}
```

- `@Counted("my.order")` : 메서드 호출마다 `my.order` 카운터를 자동 증가시킨다.
- `CountedAspect` 빈을 반드시 등록해야 AOP가 동작한다.
- `class`, `method`, `result`, `exception` 태그가 자동으로 추가된다.

---

## 2. Timer (타이머)

실행 시간을 측정한다. 카운터 기능도 포함하므로 호출 횟수와 실행 시간을 동시에 기록한다.

### Timer가 제공하는 메트릭

| 메트릭 | 설명 |
|--------|------|
| `my.order_seconds_count` | 호출 횟수 |
| `my.order_seconds_sum` | 총 실행 시간의 합 |
| `my.order_seconds_max` | 최대 실행 시간 (최근 일정 시간 기준) |

### V3: MeterRegistry를 직접 사용

`Timer.record()`를 사용하여 실행 시간을 직접 기록하는 방식이다.

```java
@Service
public class OrderServiceV3 implements OrderService {

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

        timer.record(() -> {
            stock.decrementAndGet();
            sleep(500);   // 비즈니스 로직 시뮬레이션
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
            stock.incrementAndGet();
            sleep(200);
        });
    }
}
```

- `Timer.builder("my.order")` : 타이머를 생성한다.
- `timer.record(() -> { ... })` : 람다 내부 코드의 실행 시간을 측정한다.
- 카운터와 달리 실행 시간까지 함께 기록되므로 더 풍부한 정보를 얻을 수 있다.

### V4: @Timed 애노테이션 (AOP)

`@Timed` 애노테이션을 사용하면 비즈니스 로직에 타이머 코드를 넣지 않아도 된다.

```java
@Timed("my.order")
@Service
public class OrderServiceV4 implements OrderService {

    private AtomicInteger stock = new AtomicInteger(100);

    @Override
    public void order() {
        stock.decrementAndGet();
        sleep(500);
    }

    @Override
    public void cancel() {
        stock.incrementAndGet();
        sleep(200);
    }
}
```

```java
@Configuration
public class OrderConfigV4 {
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    OrderService orderService() {
        return new OrderServiceV4();
    }
}
```

- `@Timed("my.order")` : 클래스 레벨에 붙이면 모든 public 메서드의 실행 시간을 자동 측정한다.
- `TimedAspect` 빈을 반드시 등록해야 AOP가 동작한다.
- Timer는 Counter의 기능을 포함하므로, Counter 대신 Timer를 사용하는 것이 더 많은 정보를 제공한다.

---

## 3. Gauge (게이지)

임의로 오르내릴 수 있는 현재 값을 측정한다. 재고 수량, 활성 스레드 수, 커넥션 풀 사용량 등에 사용한다.

> Counter와의 차이: Counter는 누적값만 증가하지만, Gauge는 현재 시점의 값을 그대로 보여준다.

### V1: Gauge.builder() 직접 등록

`@PostConstruct`에서 Gauge를 직접 등록하는 방식이다.

```java
@Configuration
public class StockConfigV1 {
    @Bean
    public MyStockMetric myStockMetric(OrderService orderService,
                                       MeterRegistry registry) {
        return new MyStockMetric(orderService, registry);
    }

    @RequiredArgsConstructor
    static class MyStockMetric {
        private final OrderService orderService;
        private final MeterRegistry registry;

        @PostConstruct
        public void init() {
            Gauge.builder("my.stock", orderService,
                    service -> service.getStock().get())
                    .register(registry);
        }
    }
}
```

- `Gauge.builder("my.stock", orderService, ...)` : `orderService`를 대상 객체로 지정하고, 메트릭 수집 시마다 람다를 호출하여 현재 값을 가져온다.
- Gauge는 값을 직접 증가/감소시키는 것이 아니라, 수집 시점에 콜백을 호출하여 값을 읽는다.

### V2: MeterBinder 인터페이스

스프링이 제공하는 `MeterBinder` 인터페이스를 사용하면 더 깔끔하게 Gauge를 등록할 수 있다.

```java
@Configuration
public class StockConfigV2 {
    @Bean
    public MeterBinder stockSize(OrderService orderService) {
        return registry -> Gauge.builder("my.stock",
                orderService.getStock(), AtomicInteger::get)
                .register(registry);
    }
}
```

- `MeterBinder`를 빈으로 등록하면 스프링이 자동으로 `MeterRegistry`를 주입하여 바인딩한다.
- V1보다 코드가 간결하고 스프링의 표준 패턴을 따른다.

---

## 엔드포인트

### 주문/취소 엔드포인트

```java
@RestController
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/order")
    public String order() {
        orderService.order();
        return "order";
    }

    @GetMapping("/cancel")
    public String cancel() {
        orderService.cancel();
        return "cancel";
    }

    @GetMapping("/stock")
    public int stock() {
        return orderService.getStock().get();
    }
}
```

| 엔드포인트 | 동작 | 관련 메트릭 |
|------------|------|-------------|
| `GET /order` | 주문 (재고 감소) | `my.order` (Counter/Timer) |
| `GET /cancel` | 취소 (재고 증가) | `my.order` (Counter/Timer) |
| `GET /stock` | 현재 재고 조회 | `my.stock` (Gauge) |

---

## 메인 애플리케이션 설정

```java
@Import({OrderConfigV4.class, StockConfigV2.class})
@SpringBootApplication(scanBasePackages = "hello.controller")
public class CustomMetricApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomMetricApplication.class, args);
    }
}
```

- `@Import`로 사용할 버전의 Config를 선택한다.
- 버전별로 Config 클래스를 교체하면 메트릭 구현 방식을 쉽게 변경할 수 있다.

---

## 프로메테우스에서의 메트릭 확인

커스텀 메트릭도 `/actuator/prometheus`를 통해 프로메테우스 포맷으로 노출된다.

| 마이크로미터 이름 | 프로메테우스 이름 | 설명 |
|-------------------|-------------------|------|
| `my.order` (Counter) | `my_order_total` | 주문/취소 누적 횟수 |
| `my.order` (Timer) | `my_order_seconds_count` | 호출 횟수 |
| `my.order` (Timer) | `my_order_seconds_sum` | 총 실행 시간 |
| `my.order` (Timer) | `my_order_seconds_max` | 최대 실행 시간 |
| `my.stock` (Gauge) | `my_stock` | 현재 재고 수량 |

---

## 전체 흐름 정리

```
[커스텀 메트릭 구현 방식 비교]

수동 등록 (V1, V3)
  │  MeterRegistry를 직접 사용
  │  Counter.builder() / Timer.builder() / Gauge.builder()
  │  장점: 세밀한 제어 가능
  │  단점: 비즈니스 로직에 메트릭 코드가 섞임
  ▼
AOP 기반 (V2, V4)
  │  @Counted / @Timed 애노테이션 사용
  │  CountedAspect / TimedAspect 빈 등록 필요
  │  장점: 비즈니스 로직과 메트릭 코드가 분리됨
  │  단점: AOP Aspect 빈을 별도로 등록해야 함
  ▼
Gauge
     MeterBinder 인터페이스 사용 (V2)
     수집 시점에 콜백으로 현재 값을 읽음
     증감이 아닌 현재 상태를 보여줌

[메트릭 타입 선택 가이드]

누적 횟수만 필요한가?     → Counter (@Counted)
실행 시간도 필요한가?     → Timer (@Timed) ← 권장
현재 상태값이 필요한가?   → Gauge (MeterBinder)
```
