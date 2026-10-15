package io.github.hhth24340005.gff2027.math

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Suppress("unused")
class PhasePropertyTest :
  FunSpec({
    test("fromCycles rounds the fractional part to the nearest step") {
      checkAll(ITERATIONS, finiteDoubles()) { cycles ->
        Phase.fromCycles(cycles).steps() shouldBe nearestStep(cycles)
      }
    }

    test("arithmetic wraps like integers modulo 2^32") {
      checkAll(ITERATIONS, steps(), steps(), Arb.int()) { a, b, k ->
        (phase(a) + phase(b)).steps() shouldBe
          wrap(a.toBigInteger() + b.toBigInteger())
        (phase(a) - phase(b)).steps() shouldBe
          wrap(a.toBigInteger() - b.toBigInteger())
        (-phase(a)).steps() shouldBe
          wrap(-a.toBigInteger())
        (phase(a) * k).steps() shouldBe
          wrap(a.toBigInteger() * k.toLong().toBigInteger())
      }
    }
  })

// region internal

private const val ITERATIONS = 100_000

private const val STEPS = 1L shl Int.SIZE_BITS

// Dividing by a power of two is exact, so these read the step back
private fun phase(
  steps: Long,
): Phase =
  Phase.fromCycles(steps.toDouble() / STEPS)

private fun Phase.steps(): Long =
  (toDouble() * STEPS).toLong()

private fun wrap(
  value: BigInteger,
): Long =
  value.mod(STEPS.toBigInteger()).toLong()

private fun nearestStep(
  cycles: Double,
): Long {
  val exact = BigDecimal(cycles)
  val fraction = exact - exact.setScale(0, RoundingMode.FLOOR)
  val steps =
    (fraction * BigDecimal(STEPS)).setScale(0, RoundingMode.HALF_UP)
  return wrap(steps.toBigInteger())
}

private fun finiteDoubles(): Arb<Double> =
  Arb
    .choice(Arb.double(), Arb.double(-2.0..2.0))
    .filter { it.isFinite() }

private fun steps(): Arb<Long> =
  Arb.choice(
    Arb.long(0L until STEPS),
    Arb.element(0L, 1L, STEPS / 2, STEPS - 1),
  )

// endregion
