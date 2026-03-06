package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.Set;

@Slf4j
public class CommandLineV2 {
    // 커맨드 라인 인수는 Key-value 형태를 인식하지 못하고 모두 스트링으로 인식한다.
    // 이 때 --를 붙여 키 벨류로 넣으면 스프링에서 key-value로 인식하게 만들었음
    // java -jar abc.jar --url=devdb --username=dev_user mode=on

    public static void main(String[] args) {
        for (String arg : args) {
            log.info("arg {}", arg);
        }

        ApplicationArguments appArgs = new DefaultApplicationArguments(args);
        log.info("source Args = {}", List.of(appArgs.getSourceArgs()));
        log.info("getNonOptionArgs Args = {}", appArgs.getNonOptionArgs());
        log.info("OptionsNames = {}", appArgs.getOptionNames());

        Set<String> optionNames = appArgs.getOptionNames();
        for (String optionName : optionNames) {
            log.info("Option arg {} = {}" , optionName, appArgs.getOptionValues(optionName));
        }


        List<String> url = appArgs.getOptionValues("url");
        log.info("url= {}", url);
        // 왜 반환 타입이 리스트냐면
        // --url=devdb --url=devdb2
        // 커맨드라인 옵션인수는 이렇게 두개를 받을 수 있음

    }
}
