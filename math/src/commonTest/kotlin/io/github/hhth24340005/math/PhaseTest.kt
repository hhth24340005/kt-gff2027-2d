package io.github.hhth24340005.math

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("unused")
class PhaseTest :
  FunSpec({
    test("fromCycles keeps a value within one cycle") {
      Phase.fromCycles(0.25).toDouble() shouldBe 0.25
    }

    test("fromCycles keeps only the fractional part") {
      Phase.fromCycles(1.25) shouldBe Phase.fromCycles(0.25)
      Phase.fromCycles(-0.75) shouldBe Phase.fromCycles(0.25)
      Phase.fromCycles(1.0) shouldBe Phase.zero
      Phase.fromCycles(1e300) shouldBe Phase.zero
      Phase.fromCycles(-0.0) shouldBe Phase.zero
    }

    test("fromCycles keeps the last step before a whole cycle") {
      Phase.fromCycles(1.0 - STEP).toDouble() shouldBe 1.0 - STEP
    }

    test("fromCycles wraps a value rounding up to a whole cycle") {
      Phase.fromCycles(1.0 - STEP / 4).toDouble() shouldBe 0.0
    }

    test("fromCycles rejects non-finite values") {
      shouldThrow<IllegalArgumentException> { Phase.fromCycles(Double.NaN) }
      shouldThrow<IllegalArgumentException> {
        Phase.fromCycles(Double.POSITIVE_INFINITY)
      }
      shouldThrow<IllegalArgumentException> {
        Phase.fromCycles(Double.NEGATIVE_INFINITY)
      }
    }

    test("plus wraps past the end") {
      Phase.fromCycles(0.75) + Phase.fromCycles(0.5) shouldBe
        Phase.fromCycles(0.25)
    }

    test("minus wraps past the start") {
      Phase.fromCycles(0.25) - Phase.fromCycles(0.5) shouldBe
        Phase.fromCycles(0.75)
    }

    test("unaryMinus mirrors around the start") {
      -Phase.fromCycles(0.25) shouldBe Phase.fromCycles(0.75)
      -Phase.fromCycles(0.5) shouldBe Phase.fromCycles(0.5)
      -Phase.zero shouldBe Phase.zero
    }

    test("times adds repeatedly and wraps") {
      Phase.fromCycles(0.375) * 3 shouldBe Phase.fromCycles(0.125)
      Phase.fromCycles(0.25) * 4 shouldBe Phase.zero
      Phase.fromCycles(0.25) * 0 shouldBe Phase.zero
    }

    test("times goes backward for a negative count") {
      Phase.fromCycles(0.25) * -1 shouldBe Phase.fromCycles(0.75)
    }

    test("toString shows cycles") {
      Phase.fromCycles(0.25).toString() shouldBe "Phase(0.250)"
      Phase.fromCycles(0.25).toString(precision = 2) shouldBe "Phase(0.25)"
    }

    test("toString shows zero for a phase that rounds up to a full cycle") {
      Phase.fromCycles(0.9999).toString() shouldBe "Phase(0.000)"
    }
  })

// region internal

// 2^-32, one step of a phase
private const val STEP = 1.0 / (1L shl Int.SIZE_BITS)

// endregion
