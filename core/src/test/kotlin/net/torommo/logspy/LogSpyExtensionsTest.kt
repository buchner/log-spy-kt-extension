package net.torommo.logspy.kotest

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should
import net.torommo.logspy.FakeLogSpy
import net.torommo.logspy.FakeSpyProvider
import net.torommo.logspy.ServiceLoaderWrapper
import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpyProvider
import net.torommo.logspy.spyForLogger

internal class LogSpyExtensionsTest : FreeSpec() {
    init {
        "spy by type" - {
            "creates spy for type" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                val spy = spyForLogger<ObjectA> {
                    provider.addEvent(ObjectA::class, SpiedEvent("Test", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }

                spy.events() should containExactly(SpiedEvent("Test", SpiedEvent.Level.DEBUG, null, emptyMap()))
            }

            "closes underlying spy after block" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                spyForLogger<ObjectA> {
                }

                provider.allInstancesFor(ObjectA::class).first() should beClosed()
            }

            "takes snapshot of recorded events" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                val spy = spyForLogger<ObjectA> {
                    provider.addEvent(ObjectA::class, SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }
                provider.addEvent(ObjectA::class, SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))

                spy.events() should containExactly(SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))
            }

            "creates new spy for each request" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                spyForLogger<ObjectA> {
                    provider.addEvent(ObjectA::class, SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))
                    val spy = spyForLogger<ObjectA> {
                        provider.addEvent(ObjectA::class, SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                    }

                    spy.events() should containExactly(SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }
            }

            "delayed spying" - {
                "creates spy for block" - {
                    val provider = FakeSpyProvider()
                    ServiceLoaderWrapper.predefine<SpyProvider>(provider)
                    val spy = spyForLogger<ObjectA>()
                    provider.addEvent(ObjectA::class, SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))

                    val sectionSpy = spy {
                        provider.addEvent(ObjectA::class, SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                    }

                    sectionSpy.events() should containExactly(SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }
            }
        }

        "spy by literal" - {
            "creates spy for literal" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                val spy = spyForLogger("a") {
                    provider.addEvent("a", SpiedEvent("Test", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }

                spy.events() should containExactly(SpiedEvent("Test", SpiedEvent.Level.DEBUG, null, emptyMap()))
            }

            "closes underlying spy" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                spyForLogger("a") {
                }

                provider.allInstancesFor("a").first() should beClosed()
            }

            "takes snapshot of recorded events" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                val spy = spyForLogger("a") {
                    provider.addEvent("a", SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }
                provider.addEvent("a", SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))

                spy.events() should containExactly(SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))
            }

            "creates new spy for each request" - {
                val provider = FakeSpyProvider()
                ServiceLoaderWrapper.predefine<SpyProvider>(provider)

                spyForLogger("a") {
                    provider.addEvent("a", SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))
                    val spy = spyForLogger("a") {
                        provider.addEvent("a", SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                    }

                    spy.events() should containExactly(SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }
            }

            "delayed spying" - {
                "creates spy for block" - {
                    val provider = FakeSpyProvider()
                    ServiceLoaderWrapper.predefine<SpyProvider>(provider)
                    val spy = spyForLogger("a")
                    provider.addEvent("a", SpiedEvent("Test 1", SpiedEvent.Level.DEBUG, null, emptyMap()))

                    val sectionSpy = spy {
                        provider.addEvent("a", SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                    }

                    sectionSpy.events() should containExactly(SpiedEvent("Test 2", SpiedEvent.Level.DEBUG, null, emptyMap()))
                }
            }
        }
    }
}

private fun beClosed() = object : Matcher<FakeLogSpy> {
    override fun test(value: FakeLogSpy): MatcherResult {
        return MatcherResult(
            value.isClosed(),
            {"Expected to be closed, but it's not."},
            {"Expected to be not closed, but was."}
        )
    }

}

class ObjectA;

class ObjectB;