package net.torommo.logspy

import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.RTree
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import io.kotest.property.Shrinker
import io.kotest.property.map
import io.kotest.property.rtree

/**
 * Returns an [Arb] where each random value is an array filled with elements from a provided [Gen] and with a size
 * between [minLength] and [maxLength].
 *
 * This implementation maintains the shrinking capability of the provided generator and adds shrinkings for the
 * generated array. The shrinking capability of the provided generator will be used during shrinking to generate arrays
 * with shrinked elements. At the same time shrinked arrays are generated by using the capabilities of the
 * [ArrayShrinker].
 */
inline fun <reified T> Arb.Companion.array(
    minLength: Int = 0,
    maxLength: Int = 99,
    generator: Gen<T>
): Arb<Array<T>> = object : Arb<Array<T>>() {

    init {
        check(minLength >= 0)
        check(maxLength >= 0)
        check(maxLength >= minLength)
    }

    private val arrayShrinker = ArrayShrinker<T>(minLength)

    override fun edgecases(): List<Array<T>> {
        return when (generator) {
            is Arb -> generator.edgecases()
                .asSequence()
                .map { edgecase -> Array(minLength) { _ -> edgecase} }
                .toList()
            is Exhaustive -> emptyList()
        }
    }

    override fun values(rs: RandomSource): Sequence<Sample<Array<T>>> {
        val elementSequence = generator.generate(rs).iterator()
        return generateSequence { rs.random.nextInt(minLength, maxLength + 1) }.map { size ->
            condense(generateSequence { elementSequence.next() }
                .take(size)
                .toList())
        }
    }

    private fun condense(samples: List<Sample<T>>): Sample<Array<T>> {
        val projectedValues = samples.map { it.value }.toTypedArray()
        val shrinksForProjectedValue = RTree(projectedValues, lazy {
            val shrinkedArrayElements = samples.map { sample -> sample.shrinks }
                .mapIndexed { index, shrink ->
                    shrink.map { mutatedArrayElement ->
                        val mutatedArray = projectedValues.copyOf()
                        mutatedArray[index] = mutatedArrayElement
                        mutatedArray
                    }
                }
                .toList()
            shrinkedArrayElements + arrayShrinker.rtree(projectedValues)
        })
        return Sample(projectedValues, shrinksForProjectedValue)
    }
}

/**
 * Generates shrinks of an array by leaving out array elements down to the given [minLength].
 */
class ArrayShrinker<T>(private val minLength: Int = 0) : Shrinker<Array<T>> {

    init {
        check(minLength >= 0)
    }

    override fun shrink(value: Array<T>): List<Array<T>> {
        return if (value.size <= minLength) {
           emptyList()
        } else {
            val result = mutableListOf<Array<T>>()
            result.add(value.copyOfRange(0, 1))
            result.add(value.copyOfRange(value.size - 1, value.size))
            if (value.size > 6) {
                result.add(value.copyOfRange(0, value.size / 3))
            }
            if (value.size > 9) {
                result.add(value.copyOfRange(0, value.size * 2 / 3))
            }
            if (value.size > 1) {
                result.add(value.copyOfRange(0, value.size - 1))
                result.add(value.copyOfRange(1, value.size))
            }
            result.toList()
        }
    }
}