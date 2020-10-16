package net.torommo.logspy

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.Optional
import java.util.function.Function
import kotlin.reflect.KFunction1
import kotlin.reflect.jvm.javaMethod
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Store
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.TestInstances
import org.junit.platform.commons.util.AnnotationUtils

internal class LogSpyExtensionTest : FreeSpec() {
    init {
        isolationMode = IsolationMode.InstancePerTest
        "extension" - {
            "supports" - {
                val extension = LogSpyExtension { FakeSpyProvider() }
                "spy annotated by type" - {
                    assertThat(
                        extension.supportsParameter(
                            FakeParameterContext(::withByType),
                            FakeExtensionContext(::withByType)
                        ),
                        `is`(true)
                    )
                }

                "spy annotated by literal" - {
                    assertThat(
                        extension.supportsParameter(
                            FakeParameterContext(::withByLiteral),
                            FakeExtensionContext(::withByLiteral)
                        ),
                        `is`(true)
                    )
                }
            }

            "does not support" - {
                val extension = LogSpyExtension { FakeSpyProvider() }
                "annotated spy" - {
                    assertThat(
                        extension.supportsParameter(
                            FakeParameterContext(::withoutAnnotation),
                            FakeExtensionContext(::withoutAnnotation)
                        ),
                        `is`(false)
                    )
                }

                "type annotated spy of a different type" - {
                    assertThat(
                        extension.supportsParameter(
                            FakeParameterContext(::byTypeWithNonSpyType),
                            FakeExtensionContext(::byTypeWithNonSpyType)
                        ),
                        `is`(false)
                    )
                }

                "literal annotated spy of a different type" - {
                    assertThat(
                        extension.supportsParameter(
                            FakeParameterContext(::byLiteralWithNonSpyType),
                            FakeExtensionContext(::byLiteralWithNonSpyType)
                        ),
                        `is`(false)
                    )
                }

                "spy of a subtype" - {
                    assertThat(
                        extension.supportsParameter(
                            FakeParameterContext(::withSpySubtype),
                            FakeExtensionContext(::withSpySubtype)
                        ),
                        `is`(false)
                    )
                }
            }

            "signals misconfiguration when annotated with type and literal" - {
                val extension = LogSpyExtension { FakeSpyProvider() }

                assertThrows<ParameterResolutionException> {
                    extension.supportsParameter(
                        FakeParameterContext(::withByTypeAndLiteral),
                        FakeExtensionContext(::withByTypeAndLiteral)
                    )
                }
            }

            "stores" - {
                val provider = FakeSpyProvider()
                val extension = LogSpyExtension { provider }

                "by type resolved spy" - {
                    val context = FakeExtensionContext(::withByTestType)

                    extension.resolveParameter(FakeParameterContext(::withByTestType), context)

                    val store = context.getStore(Namespace.create("net.torommo.logspy"))
                    store.get("withByTestType", Store.CloseableResource::class.java).close()
                    assertThat(
                        provider.allInstancesFor(TestClass::class).first().isClosed(),
                        `is`(true)
                    )
                }

                "by literal resolved spy" - {
                    val context = FakeExtensionContext(::withByTestLiteral)

                    extension.resolveParameter(FakeParameterContext(::withByTestLiteral), context)

                    val store = context.getStore(Namespace.create("net.torommo.logspy"))
                    store.get("withByTestLiteral", Store.CloseableResource::class.java).close()
                    assertThat(
                        provider.allInstancesFor("withByTestLiteral").first().isClosed(),
                        `is`(true)
                    )
                }
            }

            "signals" - {
                val extension = LogSpyExtension { FaultySpyProvider() }

                "resolve by type exception" - {
                    assertThrows<ParameterResolutionException> {
                        extension.resolveParameter(
                            FakeParameterContext(::withByTestType),
                            FakeExtensionContext(::withByType)
                        )
                    }
                }

                "resolve by literal exception" - {
                    assertThrows<ParameterResolutionException> {
                        extension.resolveParameter(
                            FakeParameterContext(::withByTestLiteral),
                            FakeExtensionContext(::withByTestLiteral)
                        )
                    }
                }
            }
        }
    }

    fun withByType(@ByType(Any::class) spy: LogSpy) {
    }

    fun withByTestType(@ByType(TestClass::class) spy: LogSpy) {
    }

    fun withByLiteral(@ByLiteral("SHIPS_LOG") spy: LogSpy) {
    }

    fun withByTestLiteral(@ByLiteral("withByTestLiteral") spy: LogSpy) {
    }

    fun withByTypeAndLiteral(@ByType(Any::class) @ByLiteral("CAPTAINS_LOG") spy: LogSpy) {
    }

    fun withoutAnnotation(spy: LogSpy) {
    }

    fun byTypeWithNonSpyType(@ByType(Any::class) spy: Int) {
    }

    fun byLiteralWithNonSpyType(@ByLiteral("test") spy: Int) {
    }

    fun withSpySubtype(@ByType(Any::class) spy: SubTypeSpy) {
    }

    internal interface SubTypeSpy : LogSpy {
    }

    internal class FakeExtensionContext<T>(private val target: KFunction1<T, Unit>) :
        ExtensionContext {

        private val stores = mutableMapOf<Namespace, Store>()

        override fun getElement(): Optional<AnnotatedElement> {
            return Optional.empty()
        }

        override fun getParent(): Optional<ExtensionContext> {
            return Optional.empty()
        }

        override fun getTestInstance(): Optional<Any> {
            return Optional.empty()
        }

        override fun getTestClass(): Optional<Class<*>> {
            return Optional.empty()
        }

        override fun getTestInstances(): Optional<TestInstances> {
            return Optional.empty()
        }

        override fun getDisplayName(): String {
            return "Test the test"
        }

        override fun getUniqueId(): String {
            return "08dba00d-9e0d-49c8-af7a-48b79c657d66"
        }

        override fun getRoot(): ExtensionContext {
            return this
        }

        override fun getExecutionException(): Optional<Throwable> {
            return Optional.empty()
        }

        override fun getTestMethod(): Optional<Method> {
            return Optional.ofNullable(target.javaMethod)
        }

        override fun getConfigurationParameter(key: String?): Optional<String> {
            return Optional.empty()
        }

        override fun getTestInstanceLifecycle(): Optional<Lifecycle> {
            return Optional.empty()
        }

        override fun getTags(): MutableSet<String> {
            return mutableSetOf()
        }

        override fun publishReportEntry(map: MutableMap<String, String>?) {
        }

        override fun getStore(namespace: Namespace?): Store {
            return stores.computeIfAbsent(namespace!!, { WithoutParentStore() })
        }
    }

    internal class WithoutParentStore : Store {
        private val state = mutableMapOf<Any, Any?>()

        @Suppress("UNCHECKED_CAST")
        override fun <K : Any?, V : Any?> getOrComputeIfAbsent(
            key: K,
            defaultCreator: Function<K, V>?
        ): Any {
            return state.computeIfAbsent(key as Any, defaultCreator as Function<in Any, out Any?>)
                as Any
        }

        @Suppress("UNCHECKED_CAST")
        override fun <K : Any?, V : Any?> getOrComputeIfAbsent(
            key: K,
            defaultCreator: Function<K, V>?,
            requiredType: Class<V>?
        ): V {
            return state.computeIfAbsent(key as Any, defaultCreator as Function<in Any, out Any?>)
                as V
        }

        override fun put(key: Any?, value: Any?) {
            state.put(key as Any, value)
        }

        override fun remove(key: Any?): Any {
            return state.remove(key) as Any
        }

        @Suppress("UNCHECKED_CAST")
        override fun <V : Any?> remove(key: Any?, requiredType: Class<V>?): V {
            return state.remove(key) as V
        }

        override fun get(key: Any?): Any {
            return state[key as Any] as Any
        }

        @Suppress("UNCHECKED_CAST")
        override fun <V : Any?> get(key: Any?, requiredType: Class<V>?): V {
            return state[key] as V
        }
    }

    internal class FakeParameterContext<T>(private val target: KFunction1<T, Unit>) :
        ParameterContext {

        override fun <A : Annotation?> findRepeatableAnnotations(annotationType: Class<A>?):
            MutableList<A> {
                return mutableListOf()
            }

        override fun <A : Annotation?> findAnnotation(annotationType: Class<A>?): Optional<A> {
            return AnnotationUtils.findAnnotation(getParameter(), annotationType)
        }

        override fun getParameter(): Parameter {
            return target.javaMethod!!.parameters[index]
        }

        override fun getIndex(): Int {
            return 0
        }

        override fun getTarget(): Optional<Any> {
            return Optional.empty()
        }

        override fun isAnnotated(annotationType: Class<out Annotation>?): Boolean {
            return AnnotationUtils.isAnnotated(getParameter(), annotationType)
        }
    }

    private class TestClass
}