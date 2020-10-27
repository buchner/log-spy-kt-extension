# Log Spy Kt
![Java CI](https://github.com/buchner/log-spy-kt-extension/workflows/Java%20CI/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/buchner/log-spy-kt-extension/badge.svg?branch=master)](https://coveralls.io/github/buchner/log-spy-kt-extension?branch=master)

A Kotlin-centric, Java-friendly, testing framework agnostic library for unit testing logging in the JVM.

## Disclaimer
This is a testing library under construction. Although it is thoroughly tested, API breaking changes might still occur
and it could contain serious defects. Please keep that in mind when deciding whether to use it in a production system.

## Getting Started
### Prerequisites
- JVM ≥ 8

One of the following:
- Slf4j configured with Logback as backend at runtime
- Logging to the standard output using the logstash's JSON format with
    - default configuration or
    - root cause first configuration

### Installing
Add the following dependency to your project.
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-core:0.10.2")
```
Depending on your setup, add one of the following dependencies to your project.

#### Slf4j with Logback backend
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-slf4j-logback:0.10.2")
```

#### Standard output with logstash JSON
```kotlin
testImplementation("net.torommo.logspy:log-spy-kt-logstash-stdout:0.10.2")
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
testImplementation("net.torommo.logspy:log-spy-kt-junit5-support:0.10.2")
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

Although the library is intended to be used from Kotlin it can also be used from Java. Just pick and add the dependencies
as indicated above. The usage from Java is very similar to Kotlin. The class `LogSpyJavaExtensions` in the core library
provides convenient methods for Java to generate log spies.

```java
class MyTest {
    @Test
    void aTest() {
        LogSpy spy = spyOn(Sut.class, () -> sut.doSomething());

        assertThat(spy, infos(containing(containsString("Something happened."))));
    }
}
```

Of course you can use the Junit extension from Java as well.

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

More examples for Java can be found in the `java-demo` project.

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