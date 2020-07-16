package net.torommo.logspy

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.APPEND_CHAR
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.APPEND_CHAR_SEQUENCE
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.APPEND_CHAR_SEQUENCE_WITH_RANGE
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.CHECK_ERROR
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.CLOSE
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.FLUSH
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.FORMAT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTF
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_BOOLEAN
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_CHAR_ARRAY
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_DOUBLE
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_FLOAT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_INT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_LONG
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_OBJECT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINTLN_STRING
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_BOOLEAN
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_DOUBLE
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_FLOAT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_INT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_LONG
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_OBJECT
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.PRINT_STRING
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.WRITE_BYTE_ARRAY
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.WRITE_BYTE_ARRAY_WITH_OFFSETS
import net.torommo.logspy.InterceptablePrintStreamProperty.PrintStreamActionName.WRITE_INT

class InterceptablePrintStreamProperty : FreeSpec() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerTest

    init {
        fun compareResultsOf(actions: (PrintStream) -> Unit) {
            val forwarded = ByteArrayOutputStream()
            val interceptable =
                InterceptablePrintStream(PrintStream(forwarded), ByteArrayOutputStream())
            val comparision = ByteArrayOutputStream()
            val comparisionStream = PrintStream(comparision)

            actions.invoke(interceptable)
            actions.invoke(comparisionStream)

            forwarded.toByteArray() shouldBe comparision.toByteArray()
        }
        val arbAction: Arb<PrintStreamAction> =
            arb { rs ->
                sequence {
                    val actionNameGenerator =
                        Arb.enum<PrintStreamActionName>().generate(rs).iterator()
                    val booleanGenerator = Arb.bool().generate(rs).iterator();
                    val intGenerator = Arb.int().generate(rs).iterator()
                    val doubleGenerator = Arb.double().generate(rs).iterator()
                    val longGenerator = Arb.long().generate(rs).iterator()
                    val floatGenerator = Arb.float().generate(rs).iterator()
                    val stringGenerator = Arb.string().generate(rs).iterator()
                    val charSequenceGenerator =
                        Arb.string().map { it as CharSequence }.generate(rs).iterator()
                    val charGenerator = Arb.char().generate(rs).iterator()
                    val byteArrayGenerator =
                        Arb.string().map { it.toByteArray() }.generate(rs).iterator()
                    val charArrayGenerator =
                        Arb.string().map { it.toCharArray() }.generate(rs).iterator()
                    while (true) {
                        val booleanValue = booleanGenerator.next().value
                        val intValue = intGenerator.next().value
                        val doubleValue = doubleGenerator.next().value
                        val longValue = longGenerator.next().value
                        val floatValue = floatGenerator.next().value
                        val stringValue = stringGenerator.next().value
                        val charSequenceValue = charSequenceGenerator.next().value
                        val charValue = charGenerator.next().value
                        val byteArrayValue = byteArrayGenerator.next().value
                        val charArrayValue = charArrayGenerator.next().value
                        val objectValue = Object()
                        val result: PrintStreamAction =
                            when (actionNameGenerator.next().value) {
                                PRINT_INT ->
                                    PrintStreamAction("print int $intValue") { print(intValue) }
                                PRINT_DOUBLE ->
                                    PrintStreamAction("print double $doubleValue") {
                                        print(doubleValue)
                                    }
                                PRINT_LONG ->
                                    PrintStreamAction("print long $longValue") { print(longValue) }
                                PRINT_FLOAT ->
                                    PrintStreamAction("print float $floatValue") {
                                        print(floatValue)
                                    }
                                PRINT_STRING ->
                                    PrintStreamAction("print string $stringValue") {
                                        print(stringValue)
                                    }
                                PRINT_BOOLEAN ->
                                    PrintStreamAction("print boolean $booleanValue") {
                                        print(booleanValue)
                                    }
                                PRINT_OBJECT ->
                                    PrintStreamAction("print object") { print(objectValue) }
                                WRITE_INT ->
                                    PrintStreamAction("write int $intValue") { write(intValue) }
                                WRITE_BYTE_ARRAY ->
                                    PrintStreamAction("write byte array $byteArrayValue") {
                                        write(byteArrayValue)
                                    }
                                WRITE_BYTE_ARRAY_WITH_OFFSETS ->
                                    PrintStreamAction(
                                        "write byte array $byteArrayValue with offsets"
                                    ) { write(byteArrayValue, 0, byteArrayValue.size) }
                                PRINTLN_INT ->
                                    PrintStreamAction("println int $intValue") { println(intValue) }
                                PRINTLN_DOUBLE ->
                                    PrintStreamAction("println double $doubleValue") {
                                        println(doubleValue)
                                    }
                                PRINTLN_LONG ->
                                    PrintStreamAction("println long $longValue") {
                                        println(longValue)
                                    }
                                PRINTLN_FLOAT ->
                                    PrintStreamAction("println float $floatValue") {
                                        println(floatValue)
                                    }
                                PRINTLN_STRING ->
                                    PrintStreamAction("println string $stringValue") {
                                        println(stringValue)
                                    }
                                PRINTLN_BOOLEAN ->
                                    PrintStreamAction("println boolean $booleanValue") {
                                        println(booleanValue)
                                    }
                                PRINTLN_CHAR_ARRAY ->
                                    PrintStreamAction("println char array $charArrayValue") {
                                        println(charArrayValue)
                                    }
                                PRINTLN_OBJECT ->
                                    PrintStreamAction("println object") { println(objectValue) }
                                APPEND_CHAR ->
                                    PrintStreamAction("append char $charValue") {
                                        append(charValue)
                                    }
                                APPEND_CHAR_SEQUENCE ->
                                    PrintStreamAction("append char sequence $charSequenceValue") {
                                        append(charSequenceValue)
                                    }
                                APPEND_CHAR_SEQUENCE_WITH_RANGE ->
                                    PrintStreamAction(
                                        "append char sequence $charSequenceValue with range"
                                    ) { append(charSequenceValue, 0, charSequenceValue.length) }
                                FORMAT ->
                                    PrintStreamAction("format int $intValue") {
                                        format("%d", intValue)
                                    }
                                PRINTF ->
                                    PrintStreamAction("printf int $intValue") {
                                        printf("%d", intValue)
                                    }
                                CHECK_ERROR -> PrintStreamAction("check error") { checkError() }
                                FLUSH -> PrintStreamAction("flush") { flush() }
                                CLOSE -> PrintStreamAction("close") { close() }
                            }
                        yield(result)
                    }
                }
            }
        val arbActions: Arb<PrintStreamActions> =
            arb { rs ->
                sequence {
                    val numberOfActionsGenerator = Arb.int(0, 7).generate(rs).iterator()
                    val actionGenerator = arbAction.generate(rs).iterator()
                    while (true) {
                        val numberOfActions = numberOfActionsGenerator.next().value
                        val actions =
                            actionGenerator.asSequence()
                                .take(numberOfActions)
                                .map { it.value }
                                .toList()
                        yield(PrintStreamActions(actions))
                    }
                }
            }
        "actions are transparent" - {
            checkAll(arbActions) { actions -> compareResultsOf(actions) }
        }
    }

    private enum class PrintStreamActionName {
        PRINT_INT,
        PRINT_DOUBLE,
        PRINT_LONG,
        PRINT_FLOAT,
        PRINT_STRING,
        PRINT_BOOLEAN,
        PRINT_OBJECT,
        WRITE_INT,
        WRITE_BYTE_ARRAY,
        WRITE_BYTE_ARRAY_WITH_OFFSETS,
        FLUSH,
        PRINTLN_INT,
        PRINTLN_DOUBLE,
        PRINTLN_LONG,
        PRINTLN_FLOAT,
        PRINTLN_STRING,
        PRINTLN_OBJECT,
        PRINTLN_BOOLEAN,
        PRINTLN_CHAR_ARRAY,
        APPEND_CHAR,
        APPEND_CHAR_SEQUENCE,
        APPEND_CHAR_SEQUENCE_WITH_RANGE,
        FORMAT,
        PRINTF,
        CHECK_ERROR,
        CLOSE
    }

    private class PrintStreamActions(private val actions: List<PrintStreamAction>) :
        (PrintStream) -> Unit {

        override fun invoke(stream: PrintStream) {
            actions.forEach { it.invoke(stream) }
        }

        override fun toString(): String {
            return actions.joinToString(prefix = "actions: ")
        }
    }

    private class PrintStreamAction(
        private val description: String,
        private val action: PrintStream.() -> Unit
    ) : (PrintStream) -> Unit {
        override fun invoke(stream: PrintStream) {
            action.invoke(stream)
        }

        override fun toString(): String {
            return description
        }
    }
}