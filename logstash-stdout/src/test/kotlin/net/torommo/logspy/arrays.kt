package net.torommo.logspy

import io.kotest.property.Arb
import io.kotest.property.Gen
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.arb
import kotlin.random.nextInt

inline fun <reified T> Arb.Companion.array(gen: Gen<T>, range: IntRange = 0..100): Arb<Array<T>> {
    check(!range.isEmpty())
    check(range.first >= 0)
    return arb {
        sequence {
            val genIter = gen.generate(it).iterator()
            while (true) {
                val targetSize = it.random.nextInt(range)
                val result =
                    genIter.asSequence().map { it.value }.take(targetSize).toList().toTypedArray()
                yield(result)
            }
        }
    }
}

/** Generates shrinks of an array by leaving out array elements down to the given [minLength]. */
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
