package hello;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
@Slf4j
public class JavaSystemProperties {

    public static void main(String[] args) {
        // java -Durl=devdb.com -jar abc.jar
        Properties properties = new Properties();
        for (Object key : properties.keySet()) {
            log.info("prop {} = {}", key, System.getProperty(String.valueOf(key)));

        }
    }
}
