package hello.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 단순해 보이는 코드 한줄 안에 수 많은 일들이 발생하지만 핵심은 2가지다
// 스프링 컨테이너를 생성한다.
// WAS를 생성한다.
@SpringBootApplication
public class BootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootApplication.class, args);
	}

}
