package net.torommo.logspy

import io.kotest.property.Arb
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asSequence

fun Arb.Companion.codepointCentricString(minSize: Int = 0, maxSize: Int = 100): Arb<String> {
    check(minSize >= 0)
    check(maxSize >= 0)
    check(maxSize >= minSize)
    val delegate = Arb.string(minSize, maxSize)
    val edgecases =  delegate.edgecases()
    return arb(PositionIndenpendentStringShrinker(minSize), edgecases) { rs ->
        delegate.single(rs)
    }
}

/**
 * Generates shrink of strings down to the #minLength.
 *
 * Unlike [io.kotest.property.arbitrary.StringShrinker] the shrinking happens independent of the position of the
 * codepoints in the string.
 *
 * First the length of the strings is reduced until #minLength is reached. After #minLength is reached the number of
 * different codepoints that are used in the strings is reduced.
 */
class PositionIndenpendentStringShrinker(private val minLength: Int = 0) : Shrinker<String> {

    init {
        check(minLength >= 0)
    }

    override fun shrink(value: String): List<String> {
        return if (value.length > minLength) {
            shrinksByLeftOutCodepoints(value)
        } else {
            shrinksByReducedSetOfCodepoints(value)
        }
    }

    private fun shrinksByLeftOutCodepoints(value: String): List<String> {
        if (value.length <= minLength) {
            return emptyList()
        }
        val withoutCodepoints = value.codePoints().asSequence()
            .map { filter ->
                value.codePoints().asSequence().filterNot { codepoint -> codepoint == filter }
                    .fold(StringBuilder(), StringBuilder::appendCodePoint).toString()
            }.filter { string -> string.length >= minLength }.toList()
        val withReducedCodepoints = (0..value.codePointCount(0, value.length)).asSequence()
            .map { filter ->
                value.codePoints().asSequence().withIndex().filterNot { it.index == filter }.map { it.value }
                    .fold(StringBuilder(), StringBuilder::appendCodePoint).toString()
            }
            .filter { string -> string.length >= minLength }
            .toList()

        return (withoutCodepoints + withReducedCodepoints)
            .distinct()
            .sortedBy { it.length }
    }

    private fun shrinksByReducedSetOfCodepoints(value: String): List<String> {
        val codepointFrequencyByCodepoint = value.codePoints().asSequence().groupingBy { it }.eachCount()
        val maxFrequency = codepointFrequencyByCodepoint.values.max()
        val minFrequency = codepointFrequencyByCodepoint.values.min()
        return if (minFrequency == null || maxFrequency == null || minFrequency == maxFrequency) {
            emptyList()
        } else {
            val cutoffMaxFrequency =
                max(maxFrequency * 0.9, (minFrequency + 1).toDouble())
            val cutoffMinFrequency =
                min(minFrequency * 1.3, (maxFrequency - 1).toDouble())
            val toBeReplaceds = codepointFrequencyByCodepoint.asSequence()
                .filter { entry -> entry.value >= cutoffMaxFrequency }
                .map { entry -> entry.key }
            val replacements = codepointFrequencyByCodepoint.asSequence()
                .filter { entry -> entry.value <= cutoffMinFrequency }
                .map { entry -> entry.key }
            toBeReplaceds.flatMap { toBeReplaced ->
                replacements.map { replacement ->
                    value.codePoints().asSequence()
                        .map { codepoint -> if (codepoint == toBeReplaced) replacement else codepoint }
                        .fold(StringBuilder(), StringBuilder::appendCodePoint)
                        .toString()
                }
            }.toList()
        }
    }
}