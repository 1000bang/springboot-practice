package hello.config;

import memory.MemoryController;
import memory.MemoryFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//외부 라이브러리를 빈으로 등록해야함
@Configuration
public class MemoryConfig {

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }
}

/*
그런데 라이브러리를 사용하는 클라이언트 개발자 입장을 생각해보면, 라이브러리 내부에 있는 어떤 빈을 등록해 야하는지 알아야 하고, 그것을 또 하나하나 빈으로 등록해야 한다.
지금처럼 간단한 라이브러리가 아니라 초기 설 정이 복잡하다면 사용자 입장에서는 상당히 귀찮은 작업이 될 수 있다. 이런 부분을 자동으로 처리해주는 것이 바로 스프링 부트 자동 구성(Auto Configuration)이다.
 */