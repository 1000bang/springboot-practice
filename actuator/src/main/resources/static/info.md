info 엔드포인트는 애플리케이션의 기본 정보를 노출한다.


기본으로 제공하는 기능들은 다음과 같다.
java : 자바 런타임 정보 
os : OS 정보 
env : Environment 에서 info. 로 시작하는 정보
build : 빌드 정보, META-INF/build-info.properties 파일이 필요하다.
git : git 정보, git.properties 파일이 필요하다.

env , java , os 는 기본으로 비활성화 되어 있다.


JAVA, OS 정보를 확인해보자.

```yaml
management:
  info:
    java:
      enabled: true 
    os:
      enabled: true
```

```json
{
  "java": {
    "version": "23.0.1",
    "vendor": {
      "name": "Oracle Corporation"
    },
    "runtime": {
      "name": "OpenJDK Runtime Environment",
      "version": "23.0.1+11-39"
    },
    "jvm": {
      "name": "OpenJDK 64-Bit Server VM",
      "vendor": "Oracle Corporation",
      "version": "23.0.1+11-39"
    }
  },
  "os": {
    "name": "Mac OS X",
    "version": "15.7.4",
    "arch": "aarch64"
  }
}
```



이번에는 env 를 사용해보자.
Environment 에서 info. 로 시작하는 정보를 출력한다.

```yaml
management:
  info:
    env:
      enabled: true

info:
  app:
    name: hello-actuator
    company: bang
    version: 0.0.1

```

application.yml 에서 info 로 시작하는 부분의 정보가 노출되는 것을 확인할 수 있다.
```json
{   
    "app": {
    "name": "hello-actuator",
    "company": "bang",
    "version": "0.0.1"
  }
}
```


build

이번에는 빌드 정보를 노출해보자. 빌드 정보를 노출하려면 빌드 시점에 META-INF/build-info.properties 파일을 만들어야 한다.

gradle 을 사용하면 다음 내용을 추가하면 된다.

build.gradle - 빌드 정보 추가


```groovy

springBoot { buildInfo() }

```

이렇게 하고 빌드를 해보면 build 폴더안에 resources/main/META-INF/build-info.properties 파일 을 확인할 수 있다.
build 정보는 default 옵션이기 때문에 바로 요청을 보내면 resources/main/META-INF/build-info.properties의 내용이 출력되는 것을 확인할 수 있다. 

```
  "build": {
    "artifact": "actuator",
    "name": "actuator",
    "time": "2026-03-09T05:39:55.112Z",
    "version": "0.0.1-SNAPSHOT",
    "group": "hello"
  },
```


git
앞서본 build 와 유사하게 빌드 시점에 사용한 git 정보도 노출할 수 있다. git 정보를 노출하려면 git.properties 파일이 필요하다.
물론 프로젝트가 git 으로 관리되고 있어야 한다. 그렇지 않으면 빌드시 오류가 발생한다.
build 폴더안에 resources/main/git.properties 파일을 확인할 수 있다.
```
plugins {
    ...
    id "com.gorylenko.gradle-git-properties" version "2.4.1" //git info
}
```

```
  "git": {
    "branch": "main",
    "commit": {
      "id": "96a6489",
      "time": "2026-03-08T14:42:01Z"
    }
  }
```

git에 대한 더 자세한 정보를 보고싶다면 다음 옵션을 사용하면 된다. 

application.yml 추가
```yaml
management:
    info:
        git:
            mode: "full"
```
