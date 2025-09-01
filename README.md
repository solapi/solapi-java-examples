# SOLAPI Java 예제 리스트

해당 예제는 CoolSMS 등의 SOLAPI 계열 서비스와 100% 호환되는 예제 리스트입니다.  

* 실제 사용 예제의 경우 각각 프로젝트 내의 src/main/java/com/solapi/springdemo 내의 ExampleController, KakaoExampleController를 참고해주세요.
* 예제는 스프링 부트의 버전으로 인해 JDK 17 이상으로 설정되어 있지만, 실제 사용은 JDK 1.8(8) 버전을 포함하여 그 이상의 버전에서 모두 사용하실 수 있습니다.

## 실제 개발연동 시 추가해야 할 의존성  

### Maven
```xml
<dependency>
    <groupId>com.solapi</groupId>
    <artifactId>sdk</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.solapi:sdk:1.0.3'
```

### Gradle(kotlin)
```kotlin
implementation("com.solapi:sdk:1.0.3")
```
