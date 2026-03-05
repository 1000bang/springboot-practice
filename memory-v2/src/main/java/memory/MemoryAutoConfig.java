package memory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "memory" , havingValue = "on")
public class MemoryAutoConfig {

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}

/*
여기서 끝이 아니고 자동 구성 대상 지정을 해야한다.

resources/META-INF/spring 경로에 아래 파일을 만들고
org.springframework.boot.autoconfigure.AutoConfiguration.imports

자동구성 대상을 입력한다.
memory.MemoryAutoConfig

그러면 스프링 부트는 시작 시점에 해당 파일의 정보를 읽어 자동 구성으로 사용한다.

 */