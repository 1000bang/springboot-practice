package hello.config;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class DbConfigTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionManager transactionManager;

    @Test
    void checkBean(){
        log.info("dataSource: {}", dataSource);
        log.info("jdbcTemplate: {}", jdbcTemplate);
        log.info("transactionManager: {}", transactionManager);

        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(transactionManager).isNotNull();
    }
    /*
    DbConfig에서 @Configuration 어노테이션을 삭제했는데 위 세개가 빈으로 등록되었다.
    사실 이거는 springboot에서 자동으로 bean을 등록해준것이다.

    AutoConfiguration 이라는 기능 제공 - 일반적으로 자주 사용하는 수많은 빈들을 자동으로 등록해주는 기능이다.
    spring-boot-stater에 spring-boot-autoconfigure 이라는게 들어있음 (라이브러리인듯)

    ```
        @AutoConfiguration(
        after = {DataSourceAutoConfiguration.class}
    )
    @ConditionalOnClass({DataSource.class, JdbcTemplate.class})
    @ConditionalOnSingleCandidate(DataSource.class)
    @EnableConfigurationProperties({JdbcProperties.class})
    @Import({DatabaseInitializationDependencyConfigurer.class, JdbcTemplateConfiguration.class, NamedParameterJdbcTemplateConfiguration.class})
    public class JdbcTemplateAutoConfiguration {
        public JdbcTemplateAutoConfiguration() {
        }
    }
    ```
    @AutoConfiguration : 자동 구성을 사용하려면 이 애노테이션을 등록해야 한다.
    @ConditionalOnClass({ DataSource.class, JdbcTemplate.class }) IF문과 유사한 기능을 제공한다. 이런 클래스가 있는 경우에만 설정이 동작한다. 만약 없으면 여기 있는 설 정들이 모두 무효화 되고, 빈도 등록되지 않는다.
     @Import : 스프링에서 자바 설정을 추가할 때 사용한다. @Import 의 대상이 되는 JdbcTemplateConfiguration 를 추가로 확인해보자.


     ```
     @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnMissingBean({JdbcOperations.class})
    class JdbcTemplateConfiguration {
        JdbcTemplateConfiguration() {
        }

        @Bean
        @Primary
        JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            JdbcProperties.Template template = properties.getTemplate();
            jdbcTemplate.setFetchSize(template.getFetchSize());
            jdbcTemplate.setMaxRows(template.getMaxRows());
            if (template.getQueryTimeout() != null) {
                jdbcTemplate.setQueryTimeout((int)template.getQueryTimeout().getSeconds());
            }

            return jdbcTemplate;
        }
    }
     ```

     JdbcOperations 빈이 없으면 동작한다. - JdbcTemplate 의 부모 인터페이스가 JdbcOperations 이다
     개발자가 빈을 직접 등록하면 개발자 빈을 사용하고 자동 구성은 동작하지 않는다.
     */
}