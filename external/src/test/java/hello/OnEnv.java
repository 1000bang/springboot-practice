package hello;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class OnEnv {

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        for (String key : env.keySet()) {
            log.info("env = {} = {}", key, System.getenv(key));
        }
    }
}
