package io.github.hhth24340005.gff2027.math

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

@Suppress("unused")
class Proportion2PropertyTest :
  FunSpec({
    test("keeps both components") {
      checkAll(ITERATIONS, proportions(), proportions()) { h, v ->
        val point = Proportion2(h.fromLeft, v.fromBottom)
        point.horizontal.fromLeft shouldBe h
        point.vertical.fromBottom shouldBe v
      }
    }
  })

// region internal

private const val ITERATIONS = 100_000

private const val STEPS = UInt.MAX_VALUE.toLong()

private fun proportions(): Arb<Proportion> =
  Arb
    .choice(
      Arb.long(0L..STEPS),
      Arb.element(0L, 1L, STEPS / 2, STEPS / 2 + 1, STEPS),
    ).map { Proportion.fromRateOrThrow(it.toDouble() / STEPS) }

// endregion
