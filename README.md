# SOLAPI(Purplebook, Nurigo 사이트 호환) Java 예제 프로젝트

* 실제 사용 예제의 경우 src/main/java/net/nurigo/springdemo 내의 ExampleController, KakaoExampleController를 참고해주세요.
* 해당 예제 프로젝트 외 다른 프로젝트에서 연동 작업을 진행하실 경우pom.xml 내에 okhttp3 버전을 4.9.3로 재정의 하여 사용하여야 합니다.

## 실제 개발연동 시 추가해야 할 dependency  

OkHttp3 4.9.3 버전 이상 사용할 경우 OkHttp3 의존성 제거 가능  

### Maven
```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.9.3</version>
</dependency>

<dependency>
    <groupId>net.nurigo</groupId>
    <artifactId>sdk</artifactId>
    <version>4.1.4</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.squareup.okhttp3:okhttp:4.9.3'
implementation 'net.nurigo:sdk:4.1.4'
```
