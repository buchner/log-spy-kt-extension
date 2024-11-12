package net.torommo.logspy

import net.torommo.logspy.LogSpyExtensionIntegrationTest.SetUpExtension.Companion.spyProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class LogSpyExtensionIntegrationTest {
    class SetUpExtension : BeforeAllCallback {
        companion object {
            val spyProvider: FakeSpyProvider = FakeSpyProvider()
        }

        override fun beforeAll(context: ExtensionContext?) {
            ServiceLoaderWrapper.predefine<SpyProvider>(spyProvider)
        }
    }

    @ExtendWith(SetUpExtension::class, LogSpyExtension::class)
    @Nested
    inner class ParameterInjection {
        @Test
        internal fun `injects single spy by literal`(
            @ByLiteral("TEST_LOGGER")
            spy: LogSpy,
        ) {
            spyProvider.addEvent("TEST_LOGGER", event())

            assertThat(spy.events(), contains(event()))
        }

        @ParameterizedTest
        @ValueSource(strings = ["1", "2", "3"])
        internal fun `injects single spy by literal after parameter from parameterized test`(
            parameter: String,
            @ByLiteral("TEST_LOGGER")
            spy: LogSpy,
        ) {
            spyProvider.addEvent("TEST_LOGGER", event())

            assertThat(spy.events(), contains(event()))
        }

        @Test
        internal fun `injects spies by literal with different literals`(
            @ByLiteral("TEST_LOGGER_1")
            spy1: LogSpy,
            @ByLiteral("TEST_LOGGER_2")
            spy2: LogSpy,
        ) {
            spyProvider.addEvent("TEST_LOGGER_1", event1())
            spyProvider.addEvent("TEST_LOGGER_2", event2())

            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
            )
        }

        @Test
        internal fun `injects spies by literal with equal literals`(
            @ByLiteral("TEST_LOGGER")
            spy1: LogSpy,
            @ByLiteral("TEST_LOGGER")
            spy2: LogSpy,
        ) {
            spyProvider.addEvent("TEST_LOGGER", event())

            assertAll(
                { assertThat(spy1.events(), contains(event())) },
                { assertThat(spy2.events(), contains(event())) },
            )
        }

        @Test
        internal fun `injects single spy by type`(
            @ByType(TestClass::class)
            spy: LogSpy,
        ) {
            spyProvider.addEvent(TestClass::class, event())

            assertThat(spy.events(), contains(event()))
        }

        @ParameterizedTest
        @ValueSource(strings = ["1", "2", "3"])
        internal fun `injects single spy by type after parameter from parameterized test`(
            parameter: String,
            @ByType(TestClass::class)
            spy: LogSpy,
        ) {
            spyProvider.addEvent(TestClass::class, event())

            assertThat(spy.events(), contains(event()))
        }

        @Test
        internal fun `injects spies by type with different types`(
            @ByType(TestClass1::class)
            spy1: LogSpy,
            @ByType(TestClass2::class)
            spy2: LogSpy,
        ) {
            spyProvider.addEvent(TestClass1::class, event1())
            spyProvider.addEvent(TestClass2::class, event2())

            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
            )
        }

        @Test
        internal fun `injects spies by type and literal`(
            @ByType(TestClass::class)
            spy1: LogSpy,
            @ByLiteral("TEST_LOGGER")
            spy2: LogSpy,
        ) {
            spyProvider.addEvent(TestClass::class, event1())
            spyProvider.addEvent("TEST_LOGGER", event2())

            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
            )
        }
    }

    private fun event() = SpiedEvent("Test", SpiedEvent.Level.DEBUG, null, emptyMap())

    private fun event1() = SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap())

    private fun event2() = SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap())

    private fun event3() = SpiedEvent("Test 3", SpiedEvent.Level.DEBUG, null, emptyMap())

    @ExtendWith(SetUpExtension::class, LogSpyExtension::class)
    @Nested
    inner class SingleSpyConstructorInjectionByType(
        @ByType(TestClass::class)
        val spy: LogSpy,
    ) {
        init {
            spyProvider.addEvent(TestClass::class, event())
        }

        @Test
        internal fun `injects spy`() {
            assertThat(spy.events(), contains(event()))
        }
    }

    @ExtendWith(SetUpExtension::class, LogSpyExtension::class)
    @Nested
    inner class MultipleSpiesConstructorInjectionByType(
        @ByType(TestClass1::class)
        val spy1: LogSpy,
        @ByType(TestClass2::class)
        val spy2: LogSpy,
    ) {
        init {
            spyProvider.addEvent(TestClass1::class, event1())
            spyProvider.addEvent(TestClass2::class, event2())
        }

        @Test
        internal fun `injects spies`() {
            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
            )
        }
    }

    @ExtendWith(SetUpExtension::class, LogSpyExtension::class)
    @Nested
    inner class SingleSpyConstructorInjectionByLiteral(
        @ByLiteral("TEST_LOGGER")
        val spy: LogSpy,
    ) {
        init {
            spyProvider.addEvent("TEST_LOGGER", event())
        }

        @Test
        internal fun `injects spy`() {
            assertThat(spy.events(), contains(event()))
        }
    }

    @ExtendWith(SetUpExtension::class, LogSpyExtension::class)
    @Nested
    inner class MultipleSpiesConstructorInjectionByLiteral(
        @ByLiteral("TEST_LOGGER_1")
        val spy1: LogSpy,
        @ByLiteral("TEST_LOGGER_2")
        val spy2: LogSpy,
    ) {
        init {
            spyProvider.addEvent("TEST_LOGGER_1", event1())
            spyProvider.addEvent("TEST_LOGGER_2", event2())
        }

        @Test
        internal fun `injects spy`() {
            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
            )
        }
    }

    @ExtendWith(SetUpExtension::class, LogSpyExtension::class)
    @Nested
    inner class MixedConstructorAndMethodSpyInjection(
        @ByType(TestClass1::class)
        val spy1: LogSpy,
        @ByLiteral("TEST_LOGGER_1")
        val spy2: LogSpy,
    ) {
        init {
            spyProvider.addEvent(TestClass1::class, event1())
            spyProvider.addEvent("TEST_LOGGER_1", event2())
        }

        @Test
        internal fun `injects constructor spies and by literal parameter spy`(
            @ByLiteral("TEST_LOGGER_2")
            spy3: LogSpy,
        ) {
            spyProvider.addEvent("TEST_LOGGER_2", event3())

            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
                { assertThat(spy3.events(), contains(event3())) },
            )
        }

        @Test
        internal fun `injects constructor spies and by type parameter spy`(
            @ByType(TestClass2::class)
            spy3: LogSpy,
        ) {
            spyProvider.addEvent(TestClass2::class, event3())

            assertAll(
                { assertThat(spy1.events(), contains(event1())) },
                { assertThat(spy2.events(), contains(event2())) },
                { assertThat(spy3.events(), contains(event3())) },
            )
        }
    }

    class TestClass

    class TestClass1

    class TestClass2
}
