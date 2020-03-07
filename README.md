# Log Spy Kt
![Java CI](https://github.com/buchner/log-spy-kt-extension/workflows/Java%20CI/badge.svg)

A Kotlin-centric, Java-friendly library for unit testing logging.

## Disclaimer
This is an experimental extensions for JUnit. In its current state it is not intended nor suited for productive use.

## Getting Started
### Prerequisites
- JUnit 5
- JVM ≥ 8

One of the following:
- Slf4j configured with Logback as backend at runtime
- Logging to the standard output using the logstash's JSON format including root cause first configuration

### Installing
Depending on your setup, add one of the following dependencies to your project.

#### Slf4j with Logback backend
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-slf4j-logback:0.8.0")
```

#### Standard ouput with logstash JSON
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-logstash-stdout:0.8.0")
```

Now you are ready to use the extension.
```kotlin
@ExtendWith(LogSpyExtension::class)
internal class MyTest {
    @Test
    internal fun aTest(@ByType(Sut::class) spy: LogSpy) {
        // do something
        assertThat(spy.warnings(), hasItem("Something happened."))
    }
}
```
For more information consult the KDoc of the files.

#### Usage from Java

Java is not officially supported but it can be used from it. Just pick and add a dependency as indicated above. The
usage from Java is very similar to Kotlin.

```java
@ExtendWith(LogSpyExtension.class)
class MyTest {
    @Test
    void aTest(@ByType(Sut.class) LogSpy spy) {
        // do something
        assertThat(spy.warnings(), hasItem("Something happened."));
    }
}
```

A more complete example can be found in the `java-demo` project.

#### Known limitations
- Nested objects in the mdc are not supported
- When the standard out is used
    - markers will be treated as mdc values,
    - multiline exception messages can cause that frames are parsed incorrectly.

### Running the tests
```shell script
gradle test
```

### License
This project is licensed under LGPLv3 - see the [LICENSE.md](LICENSE.md) file for details.