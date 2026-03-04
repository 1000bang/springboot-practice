package hello;

import hello.boot.MySpringApplication;
import hello.boot.MySpringBootApplication;

@MySpringBootApplication
public class MySpringBootMain {
    public static void main(String[] args) {
        System.out.println("MySpringBootMain.main");
        MySpringApplication.run(MySpringBootMain.class, args);
    }

}

// 스프링 부트는 지금까지 문제를 해결한다.
// 내장 톰캣을 사용해서 빌드와 배포를 편리하게 하고
// 빌드시 하나의 Jar를 사용하면서 동시에 Fat Jar 문제도 해결한다.
// 지금까지 진행한 내장톰캣 서버를 실행하기 위한 복잡한 과정을 자동으로 처리한다.