package hello.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    /*
    memory-v1은 순수 자바 라이브러리
    memory-v1.jar 는 스스로 동작하지는 못하고 다른 곳에 포함되어서 동작하는 라이브러리이다. 이제 이 라이브러리 프로젝트v1에서 사용해보자.

     */
    @GetMapping("/hello")
    public String hello () {
        return "hello";
    }
}
