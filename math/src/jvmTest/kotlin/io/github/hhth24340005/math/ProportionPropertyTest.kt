package io.github.hhth24340005.math

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.roundToLong

@Suppress("unused")
class ProportionPropertyTest :
  FunSpec({
    test("keeps its step through toDouble") {
      checkAll(ITERATIONS, steps()) { n ->
        proportion(n).steps() shouldBe n
      }
    }

    test("times rounds the exact product") {
      checkAll(ITERATIONS, steps(), steps()) { a, b ->
        (proportion(a) * proportion(b)).steps() shouldBe
          divideByOneRounded(a.toBigInteger() * b.toBigInteger())
      }
    }

    test("lerp rounds the exact interpolation") {
      checkAll(ITERATIONS, steps(), steps(), steps()) { a, b, t ->
        proportion(a).lerp(proportion(b), proportion(t)).steps() shouldBe
          divideByOneRounded(
            a.toBigInteger() *
              (STEPS - t).toBigInteger() +
              b.toBigInteger() *
              t.toBigInteger(),
          )
      }
    }
  })

// region internal

private const val ITERATIONS = 100_000

private const val STEPS = UInt.MAX_VALUE.toLong()

private fun proportion(
  steps: Long,
): Proportion =
  Proportion.fromRateOrThrow(steps.toDouble() / STEPS)

private fun Proportion.steps(): Long =
  (toDouble() * STEPS).roundToLong()

private fun divideByOneRounded(
  scaled: BigInteger,
): Long =
  BigDecimal(scaled)
    .divide(BigDecimal(STEPS), 0, RoundingMode.HALF_UP)
    .toLong()

private fun steps(): Arb<Long> =
  Arb.choice(
    Arb.long(0L..STEPS),
    Arb.element(0L, 1L, STEPS / 2, STEPS / 2 + 1, STEPS),
  )

// endregion
