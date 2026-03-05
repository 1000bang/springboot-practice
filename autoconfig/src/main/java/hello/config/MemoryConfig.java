package hello.config;

import memory.MemoryController;
import memory.MemoryFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class MemoryConfig {
    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }

    @Bean
    public MemoryFinder memoryFinder(){
        return  new MemoryFinder();
    }
}

/*
memory라는 완전 별도 모듈을 Bean으로 직접 등록하면 해당 모듈을 기능을 hello 에서 불러다 사용할 수 있다.

 */