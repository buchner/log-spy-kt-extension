package net.torommo.logspy

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Store
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.TestInstances
import org.junit.jupiter.engine.execution.ExtensionValuesStore
import org.junit.jupiter.engine.execution.NamespaceAwareStore
import org.junit.platform.commons.util.AnnotationUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.Optional
import kotlin.reflect.KFunction1
import kotlin.reflect.jvm.javaMethod

internal class LogSpyExtensionTest {

    @Test
    internal fun `supports spy annotated by type`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThat(extension.supportsParameter(FakeParameterContext(this::withByType), FakeExtensionContext(this::withByType)), `is`(true))
    }

    @Test
    internal fun `supports spy annotated by literal`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThat(extension.supportsParameter(FakeParameterContext(this::withByLiteral), FakeExtensionContext(this::withByLiteral)), `is`(true))    }

    @Test
    internal fun `does not support not annotated spy`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThat(extension.supportsParameter(FakeParameterContext(this::withoutAnnotation), FakeExtensionContext(this::withoutAnnotation)), `is`(false))
    }

    @Test
    internal fun `does not support type annotated spy of a different type`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThat(extension.supportsParameter(FakeParameterContext(this::byTypeWithNonSpyType), FakeExtensionContext(this::byTypeWithNonSpyType)), `is`(false))
    }

    @Test
    internal fun `does not support literal annotated spy of a different type`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThat(extension.supportsParameter(FakeParameterContext(this::byLiteralWithNonSpyType), FakeExtensionContext(this::byLiteralWithNonSpyType)), `is`(false))
    }

    @Test
    internal fun `does not support spy of a subtype`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThat(extension.supportsParameter(FakeParameterContext(this::withSpySubtype), FakeExtensionContext(this::withSpySubtype)), `is`(false))
    }

    @Test
    internal fun `signals misconfiguration when annoted with type and literal`() {
        val extension = LogSpyExtension(FakeSpyProvider())

        assertThrows<ParameterResolutionException> {
            extension.supportsParameter(FakeParameterContext(this::withByTypeAndLiteral), FakeExtensionContext(this::withByTypeAndLiteral))
        }
    }

    @Test
    internal fun `stores by type resolved spy`() {
        val provider = FakeSpyProvider()
        val spy = FakeLogSpy()
        provider.register(TestClass::class, spy)
        val extension = LogSpyExtension(provider)
        val context = FakeExtensionContext(this::withByTestType)

        extension.resolveParameter(FakeParameterContext(this::withByTestType), context)

        val store = context.getStore(Namespace.create("net.torommo.logspy"))
        store.get("withByTestType", Store.CloseableResource::class.java).close()
        assertThat(spy.isClosed(), `is`(true))
    }

    @Test
    internal fun `stores by literal resolved spy`() {
        val provider = FakeSpyProvider()
        val spy = FakeLogSpy()
        provider.register("withByTestLiteral", spy)
        val extension = LogSpyExtension(provider)
        val context = FakeExtensionContext(this::withByTestLiteral)

        extension.resolveParameter(FakeParameterContext(this::withByTestLiteral), context)

        val store = context.getStore(Namespace.create("net.torommo.logspy"))
        store.get("withByTestLiteral", Store.CloseableResource::class.java).close()
        assertThat(spy.isClosed(), `is`(true))
    }

    @Test
    internal fun `signals resolve by type exception`() {
        val extension = LogSpyExtension(FaultySpyProvider())

        assertThrows<ParameterResolutionException> {
            extension.resolveParameter(FakeParameterContext(this::withByTestType), FakeExtensionContext(this::withByType))
        }
    }

    @Test
    internal fun `signals resolve by literal exception`() {
        val extension = LogSpyExtension(FaultySpyProvider())

        assertThrows<ParameterResolutionException> {
            extension.resolveParameter(FakeParameterContext(this::withByTestLiteral), FakeExtensionContext(this::withByTestLiteral))
        }
    }

    fun withByType(@ByType(Any::class) spy : LogSpy) {
    }

    fun withByTestType(@ByType(TestClass::class) spy : LogSpy) {
    }

    fun withByLiteral(@ByLiteral("SHIPS_LOG") spy : LogSpy) {
    }

    fun withByTestLiteral(@ByLiteral("withByTestLiteral") spy : LogSpy) {
    }

    fun withByTypeAndLiteral(@ByType(Any::class) @ByLiteral("CAPTAINS_LOG") spy : LogSpy) {
    }

    fun withoutAnnotation(spy : LogSpy) {
    }

    fun byTypeWithNonSpyType(@ByType(Any::class) spy : Int) {
    }

    fun byLiteralWithNonSpyType(@ByLiteral("test") spy : Int) {
    }

    fun withSpySubtype(@ByType(Any::class) spy : SubTypeSpy) {
    }

    internal interface SubTypeSpy : LogSpy {
    }

    internal class FakeExtensionContext<T>(private val target: KFunction1<T, Unit>) : ExtensionContext {
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
            return stores.computeIfAbsent(namespace!!, { item -> NamespaceAwareStore(ExtensionValuesStore(null), item) })
        }
    }

    internal class FakeParameterContext<T>(private val target: KFunction1<T, Unit>) : ParameterContext {
        override fun <A : Annotation?> findRepeatableAnnotations(annotationType: Class<A>?): MutableList<A> {
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

    private class TestClass {}
}