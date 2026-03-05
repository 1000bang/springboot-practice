package hello.selector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportSelectorTest {

    @Test
    void staticConfig(){
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(StaticConfig.class);
        HelloBean bean = ac.getBean(HelloBean.class);
        assertThat(bean).isNotNull();
    }

    @Configuration
    @Import(HelloConfig.class)
    public static class StaticConfig{

    }


    //동적인 방식
    @Test
    void selectorConfig(){
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SelectorConfig.class);
        HelloBean bean = ac.getBean(HelloBean.class);
        assertThat(bean).isNotNull();
    }

    @Configuration
    @Import(HelloImportSelector.class)
    public static class SelectorConfig{

    }
}

/*
스프링 부트 자동 구성이 동작하는 방식은 다음 순서로 확인할 수 있다.
@SpringBootApplication ->
@EnableAutoConfiguration ->

@Import(AutoConfigurationImportSelector.class) ->
resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 파일을 열어서 설정 정보 선택
해당 파일의 설정 정보가 스프링 컨테이너에 등록되고 사용
 */