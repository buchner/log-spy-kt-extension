# Log Spy Kt
A Kotlin-centric, Java-friendly library for unit testing logging.

## Disclaimer
This is an experimental extensions for JUnit. In its current state it is not intended nor suited for productive use.

## Getting Started
### Prerequisites
- JUnit 5
- JVM ≥ 8
- Slf4j configured with Logback as backend at runtime

### Installing
Add the following dependency to your project.

```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-slf4j-logback:0.8.0")
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

### Running the tests
```shell script
gradle test
```

### License
This project is licensed under LGPLv3 - see the [LICENSE.md](LICENSE.md) file for details.