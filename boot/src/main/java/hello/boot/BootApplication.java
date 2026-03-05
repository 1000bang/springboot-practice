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

/*
./gradlew clean build 수행 후
jar파일을 압축을 풀어보니
BOOT-INT 안에 classes와 lib 외부 라이브러리 폴더가 생성됨

jar를 푼 결과를 보면 Fat jar가 아니라 새로운 구조로 만들어져 있다. jar 내부에 Jar를 담아서 인식하는 것이 불가능한데
jar가 포함되어 있고 인식까지 되었다.

-- 스프링 부트의 실행 가능 Jar
스프링 부트는 이런 문제를 해결하기 위해 Jar 내부에 jar를 포함할 수 있는 특별한 구조의 jar를 만들고
동시에 만든 Jar 내부 Jar를 포함해서 실행 할 수 있게 했다.
이것을 실행 가능 Jar (Executable Jar) 라 한다.

문제: 어떤 라이브러리가 포함되어 있는지 확인하기 어렵다.
해결: jar 내부에 jar를 포함하기 때문에 어떤 라이브러리가 포함되어 있는지 쉽게 확인할 수 있다.

문제: 파일명 중복을 해결할 수 없다.
해결: jar 내부에 jar를 포함하기 때문에 a.jar , b.jar 내부에 같은 경로의 파일이 있어도 둘다 인식할 수 있다.

실행 가능 Jar는 자바 표준은 아니고, 스프링 부트에서 새롭게 정의한 것이다.

 */