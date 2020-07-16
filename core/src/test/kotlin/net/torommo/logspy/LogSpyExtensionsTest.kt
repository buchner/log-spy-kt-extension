package net.torommo.logspy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should

internal class LogSpyExtensionsTest : FreeSpec() {
    init {
        "spy by type" - {
            "creates spy for type" - {
                useFakeSpyProvider { provider ->
                    val spy = spyOn<TestObject> { provider.addEvent(TestObject::class, event()) }

                    spy.events() should containExactly(event())
                }
            }

            "closes underlying spy after block" - {
                useFakeSpyProvider { provider ->
                    spyOn<TestObject> {}

                    provider.allInstancesFor(TestObject::class).forAll { it.shouldBeClosed() }
                }
            }

            "closes underlying spy after faulty block" - {
                useFakeSpyProvider { provider ->
                    shouldThrow<java.lang.RuntimeException> {
                        spyOn<TestObject> { throw RuntimeException("Test exception") }
                    }

                    provider.allInstancesFor(TestObject::class).forAll { it.shouldBeClosed() }
                }
            }

            "takes snapshot of recorded events" - {
                useFakeSpyProvider { provider ->
                    val spy = spyOn<TestObject> { provider.addEvent(TestObject::class, event1()) }
                    provider.addEvent(TestObject::class, event2())

                    spy.events() should containExactly(event1())
                }
            }

            "creates new spy for each request" - {
                useFakeSpyProvider { provider ->
                    spyOn<TestObject> {
                        provider.addEvent(TestObject::class, event1())
                        val spy =
                            spyOn<TestObject> { provider.addEvent(TestObject::class, event2()) }

                        spy.events() should containExactly(event2())
                    }
                }
            }

            "delayed spying" - {
                "creates spy for block" - {
                    useFakeSpyProvider { provider ->
                        val spy = spyForLogger<TestObject>()
                        provider.addEvent(TestObject::class, event1())

                        val sectionSpy = spy { provider.addEvent(TestObject::class, event2()) }

                        sectionSpy.events() should containExactly(event2())
                    }
                }
            }
        }

        "spy by literal" - {
            "creates spy for literal" - {
                useFakeSpyProvider { provider ->
                    val spy = spyOn("a") { provider.addEvent("a", event()) }

                    spy.events() should containExactly(event())
                }
            }

            "closes underlying spy" - {
                useFakeSpyProvider { provider ->
                    spyOn("a") {}

                    provider.allInstancesFor("a").forAll { it.shouldBeClosed() }
                }
            }

            "closes underlying spy after faulty block" - {
                useFakeSpyProvider { provider ->
                    shouldThrow<java.lang.RuntimeException> {
                        spyOn("a") { throw RuntimeException("Test exception") }
                    }

                    provider.allInstancesFor("a").forAll { it.shouldBeClosed() }
                }
            }

            "takes snapshot of recorded events" - {
                useFakeSpyProvider { provider ->
                    val spy = spyOn("a") { provider.addEvent("a", event1()) }
                    provider.addEvent("a", event2())

                    spy.events() should containExactly(event1())
                }
            }

            "creates new spy for each request" - {
                useFakeSpyProvider { provider ->
                    spyOn("a") {
                        provider.addEvent("a", event1())
                        val spy = spyOn("a") { provider.addEvent("a", event2()) }

                        spy.events() should containExactly(event2())
                    }
                }
            }

            "delayed spying" - {
                "creates spy for block" - {
                    useFakeSpyProvider { provider ->
                        val spy = spyForLogger("a")
                        provider.addEvent("a", event1())

                        val sectionSpy = spy { provider.addEvent("a", event2()) }

                        sectionSpy.events() should containExactly(event2())
                    }
                }
            }
        }
    }
}

private fun useFakeSpyProvider(block: (FakeSpyProvider) -> Unit) {
    val provider = FakeSpyProvider()
    ServiceLoaderWrapper.predefine<SpyProvider>(provider)
    block(provider)
}

private fun event() = SpiedEvent("Test", SpiedEvent.Level.DEBUG, null, emptyMap())
private fun event1() = SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap())
private fun event2() = SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap())

private fun beClosed() = object : Matcher<FakeLogSpy> {
    override fun test(value: FakeLogSpy): MatcherResult {
        return MatcherResult(
            value.isClosed(),
            { "Expected to be closed, but it was not." },
            { "Expected to be not closed, but was." }
        )
    }
}

private fun FakeLogSpy.shouldBeClosed() = this should beClosed()

class TestObject;