package io.github.hhth24340005.math

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.math.nextUp

@Suppress("unused")
class ProportionTest :
  FunSpec({
    // Above 0.5, so the stored bits have the sign bit set
    val high = Proportion.fromRateOrThrow(0.75)
    val low = Proportion.fromRateOrThrow(0.25)

    test("zero and one are exact") {
      Proportion.zero.toDouble() shouldBe 0.0
      Proportion.one.toDouble() shouldBe 1.0
    }

    test("fromRateOrThrow keeps both ends") {
      Proportion.fromRateOrThrow(0.0) shouldBe Proportion.zero
      Proportion.fromRateOrThrow(1.0) shouldBe Proportion.one
      Proportion.fromRateOrThrow(-0.0) shouldBe Proportion.zero
    }

    test("fromRateOrThrow rounds to the nearest step") {
      Proportion.fromRateOrThrow(0.75).toDouble() shouldBe
        (0.75 plusOrMinus HALF_STEP)
    }

    test("fromRateOrThrow reads back what toDouble returns") {
      Proportion.fromRateOrThrow(high.toDouble()) shouldBe high
    }

    test("fromRateOrThrow rejects values outside 0..1") {
      shouldThrow<IllegalArgumentException> {
        Proportion.fromRateOrThrow(-Double.MIN_VALUE)
      }
      shouldThrow<IllegalArgumentException> {
        Proportion.fromRateOrThrow(1.0.nextUp())
      }
      shouldThrow<IllegalArgumentException> {
        Proportion.fromRateOrThrow(Double.NaN)
      }
    }

    test("compares above 0.5 as larger") {
      (high > low) shouldBe true
      (Proportion.one > Proportion.zero) shouldBe true
    }

    test("times keeps one and zero") {
      high * Proportion.one shouldBe high
      high * Proportion.zero shouldBe Proportion.zero
    }

    test("times rounds to the nearest step") {
      (high * high).toDouble() shouldBe (0.5625 plusOrMinus STEP)
    }

    test("complement swaps zero and one") {
      Proportion.zero.complement shouldBe Proportion.one
      Proportion.one.complement shouldBe Proportion.zero
    }

    test("complement adds up to one") {
      high.complement.complement shouldBe high
      (high.toDouble() + high.complement.toDouble()) shouldBe
        (1.0 plusOrMinus 1e-15)
    }

    test("lerp keeps both ends") {
      low.lerp(high, Proportion.zero) shouldBe low
      low.lerp(high, Proportion.one) shouldBe high
    }

    test("lerp from zero to one returns t") {
      Proportion.zero.lerp(Proportion.one, high) shouldBe high
    }

    test("lerp from one to zero returns the complement of t") {
      Proportion.one.lerp(Proportion.zero, high) shouldBe high.complement
    }

    test("lerp between equal values returns them") {
      high.lerp(high, low) shouldBe high
    }

    test("coerceAtMost and coerceAtLeast pick by value") {
      high.coerceAtMost(low) shouldBe low
      low.coerceAtMost(high) shouldBe low
      high.coerceAtLeast(low) shouldBe high
      low.coerceAtLeast(high) shouldBe high
    }

    test("coerceIn clamps into the range") {
      Proportion.one.coerceIn(low, high) shouldBe high
      Proportion.zero.coerceIn(low, high) shouldBe low
      high.coerceIn(low, Proportion.one) shouldBe high
      high.coerceIn(low, low) shouldBe low
    }

    test("coerceIn rejects an empty range") {
      shouldThrow<IllegalArgumentException> { low.coerceIn(high, low) }
    }

    test("toString shows percent") {
      low.toString() shouldBe "25.0%"
      Proportion.one.toString(precision = 0) shouldBe "100%"
    }
  })

// region internal

// 1 / (2^32 - 1), one step of a proportion
private const val STEP = 1.0 / UInt.MAX_VALUE.toDouble()

private const val HALF_STEP = STEP / 2

// endregion
