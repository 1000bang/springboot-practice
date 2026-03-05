package hello.config;

import memory.MemoryCondition;
import memory.MemoryController;
import memory.MemoryFinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
//@Conditional(MemoryCondition.class) springboot는 이미 이런걸 만들어 놨다. ConditionalOnProperty
@ConditionalOnProperty(name="memory", havingValue="on")
public class MemoryConfig2 {
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
VM Option에서 -Dmemory=on 을 넣어주지 않으면 얘네들이 bean에 등록되지 않아서 기능이 동작하지 않는다.

Conditional 은 if 문이다
matches() 메서드가 true 를 반환하면 조건에 만족해서 동작하고, false 를 반환하면 동작하지 않는다.


@Conditional 은 스프링 프레임워크
@ConditionalOnXxxx는  스프링 부트 제공
 */

/*
@ConditionalOnXxx

스프링은 @Conditional 과 관련해서 개발자가 편리하게 사용할 수 있도록 수 많은 @ConditionalOnXxx 를 제공 한다.

대표적인 몇가지를 알아보자.

@ConditionalOnClass / @ConditionalOnMissingClass
클래스가 있는 경우 동작한다. / 그 반대

@ConditionalOnBean / @ConditionalOnMissingBean
빈이 등록되어 있는 경우 동작한다 / 그 반대

@ConditionalOnProperty
환경 정보가 있는 경우 동작한다.

@ConditionalOnResource
리소스가 있는 경우 동작한다.

@ConditionalOnWebApplication , @ConditionalOnNotWebApplication
웹 애플리케이션인 경우 동작한다.

@ConditionalOnExpression
SpEL 표현식에 만족하는 경우 동작한다.

ConditionalOnXxx 공식 메뉴얼
https://docs.spring.io/spring-boot/docs/current/reference/html/ features.html#features.developing-auto-configuration.condition-annotations
 */