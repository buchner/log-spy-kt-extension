# Log Spy Kt
![Java CI](https://github.com/buchner/log-spy-kt-extension/workflows/Java%20CI/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/buchner/log-spy-kt-extension/badge.svg?branch=master)](https://coveralls.io/github/buchner/log-spy-kt-extension?branch=master)

A Kotlin-centric, Java-friendly, testing framework agnostic library for unit testing logging in the JVM.

## Disclaimer
This is an experimental testing library. In its current state it is not intended nor suited for productive use.

## Getting Started
### Prerequisites
- JVM ≥ 8

One of the following:
- Slf4j configured with Logback as backend at runtime
- Logging to the standard output using the logstash's JSON format including root cause first configuration

### Installing
Add the following dependency to your project.
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-core:0.8.0")
```
Depending on your setup, add one of the following dependencies to your project.

#### Slf4j with Logback backend
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-slf4j-logback:0.8.0")
```

#### Standard ouput with logstash JSON
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-logstash-stdout:0.8.0")
```

Now you are ready to use the library. E.g. in Junit 5 you could write the following.
```kotlin
internal class MyTest {
    @Test
    internal fun aTest() {
        val spy = spyForLogger<Sut> {
            // do something
        }

        assertThat(spy.warnings(), hasItem("Something happened."))
    }
}
```
For more information consult the KDoc of the files or have a look into the tests.

#### Junit 5 support
When you are using Junit 5 you might favor injection over the explicit creation of the log spies. For that the library
provides a Junit 5 extension. For using the extension add the following dependency.
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-junit5-support:0.8.0")
```
Now you can let Junit 5 inject the log spies. E.g. you could write the following.
```kotlin
internal class MyTest {
    @Test
    internal fun aTest(@ByType(Sut::class) spy: LogSpy) {
        // do something

        assertThat(spy.warnings(), hasItem("Something happened."))
    }
}
```

For more information consult the KDoc of the files or have a look into the tests.
#### Usage from Java

Java is not officially supported by the library but it can be used from it. Just pick and add the dependencies as
indicated above. For Java, we recommend using the Junit 5 extension. While it is possible to use the log spy functions
from Java their readability is poor. The usage from Java with the Junit 5 extension is very similar to Kotlin.

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