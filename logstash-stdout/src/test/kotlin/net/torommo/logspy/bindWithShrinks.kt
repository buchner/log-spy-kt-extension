package net.torommo.logspy

import io.kotest.property.Arb
import io.kotest.property.Gen
import io.kotest.property.RTree
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import io.kotest.property.map

/**
 * Returns an [Arb] whose values are a projection of two values from generators.
 *
 * Unlike [io.kotest.property.arbitrary.bind] this implementation preserves the shrinking capabilities of the
 * generators. The shrinks of the generators are used in combination with [bindFn] to generate shrinked projects.
 */
fun <A, B, T> Arb.Companion.bindWithShrinks(genA: Gen<A>, genB: Gen<B>, bindFn: (A, B) -> T): Arb<T> =
    object : Arb<T>() {
        override fun edgecases(): List<T> {
            return emptyList();
        }

        override fun values(rs: RandomSource): Sequence<Sample<T>> {
            val sequenceA = genA.generate(rs).iterator()
            val sequenceB = genB.generate(rs).iterator()
            return generateSequence { Pair(sequenceA.next(), sequenceB.next()) }
                .map {
                    Sample(
                        bindFnWith(it),
                        RTree(
                            bindFnWith(it),
                            lazy { withFirstShrinks(it).children.value + withSecondShrinks(it).children.value })
                    )
                }
        }

        private fun withFirstShrinks(pair: Pair<Sample<A>, Sample<B>>) =
            pair.first.shrinks.map {
                bindFn(
                    it,
                    pair.second.value
                )
            }

        private fun withSecondShrinks(pair: Pair<Sample<A>, Sample<B>>) =
            pair.second.shrinks.map {
                bindFn(
                    pair.first.value,
                    it
                )
            }

        private fun bindFnWith(pair: Pair<Sample<A>, Sample<B>>): T =
            bindFn(pair.first.value, pair.second.value)
    }

/**
 * Returns an [Arb] whose values are a projection of four values from generators.
 *
 * Unlike [io.kotest.property.arbitrary.bind] this implementation preserves the shrinking capabilities of the
 * generators. The shrinks of the generators are used in combination with [bindFn] to generate shrinked projects.
 */
fun <A, B, C, D, T> Arb.Companion.bindWithShrinks(
    genA: Gen<A>, genB: Gen<B>, genC: Gen<C>, genD: Gen<D>,
    bindFn: (A, B, C, D) -> T
): Arb<T> = object : Arb<T>() {
    override fun edgecases(): List<T> {
        return emptyList()
    }

    override fun values(rs: RandomSource): Sequence<Sample<T>> {
        val sequenceA = genA.generate(rs).iterator()
        val sequenceB = genB.generate(rs).iterator()
        val sequenceC = genC.generate(rs).iterator()
        val sequenceD = genD.generate(rs).iterator()
        return generateSequence {
            Quadruple(
                sequenceA.next(),
                sequenceB.next(),
                sequenceC.next(),
                sequenceD.next()
            )
        }.map {
            Sample(
                bindFnWith(it),
                RTree(
                    bindFnWith(it),
                    lazy { withFirstShrinks(it).children.value +
                            withSecondShrinks(it).children.value +
                            withThirdShrinks(it).children.value +
                            withFourthShrinks(it).children.value })
            )
        }
    }

    private fun withFirstShrinks(quadruple: Quadruple<Sample<A>, Sample<B>, Sample<C>, Sample<D>>) =
        quadruple.first.shrinks.map {
            bindFn(
                it,
                quadruple.second.value,
                quadruple.third.value,
                quadruple.fourth.value
            )
        }

    private fun withSecondShrinks(quadruple: Quadruple<Sample<A>, Sample<B>, Sample<C>, Sample<D>>) =
        quadruple.second.shrinks.map {
            bindFn(
                quadruple.first.value,
                it,
                quadruple.third.value,
                quadruple.fourth.value
            )
        }

    private fun withThirdShrinks(quadruple: Quadruple<Sample<A>, Sample<B>, Sample<C>, Sample<D>>) =
        quadruple.third.shrinks.map {
            bindFn(
                quadruple.first.value,
                quadruple.second.value,
                it,
                quadruple.fourth.value
            )
        }

    private fun withFourthShrinks(quadruple: Quadruple<Sample<A>, Sample<B>, Sample<C>, Sample<D>>) =
        quadruple.fourth.shrinks.map {
            bindFn(
                quadruple.first.value,
                quadruple.second.value,
                quadruple.third.value,
                it
            )
        }

    private fun bindFnWith(quadruple: Quadruple<Sample<A>, Sample<B>, Sample<C>, Sample<D>>): T =
        bindFn(quadruple.first.value, quadruple.second.value, quadruple.third.value, quadruple.fourth.value)
}

private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)