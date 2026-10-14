package io.github.hhth24340005.gff2027.math

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.pow

@Suppress("unused")
class FormatPropertyTest :
  FunSpec({
    val precisions = Arb.int(0..MAX_PRECISION)

    test("Double matches String.format") {
      checkAll(ITERATIONS, finiteDoubles(), precisions) { value, precision ->
        value.toString(precision = precision) shouldBe
          String
            .format(Locale.ROOT, "%.${precision}f", value)
            .withoutNegativeZero()
      }
    }

    // String.format widens a Float to Double, so compare with its digits
    test("Float matches rounding Float.toString") {
      checkAll(ITERATIONS, finiteFloats(), precisions) { value, precision ->
        value.toString(precision = precision) shouldBe
          BigDecimal(value.toString())
            .setScale(precision, RoundingMode.HALF_UP)
            .toPlainString()
            .withoutNegativeZero()
      }
    }

    // Its upper midpoint is exactly 10^23, which reads back as this value
    test("1e23 uses the midpoint of its even significand") {
      1e23.toString(precision = 0) shouldBe "100000000000000000000000"
    }
  })

// region internal

private const val ITERATIONS = 100_000

private const val MAX_PRECISION = 20

private const val MAX_DOUBLE_DIGITS = 999_999_999_999_999L

private const val MAX_DOUBLE_SCALE = 15

private const val MAX_FLOAT_DIGITS = 9_999_999

private const val MAX_FLOAT_SCALE = 7

// Short decimals such as 1.15 sit near ties, unlike most random bits
private fun finiteDoubles(): Arb<Double> =
  Arb.choice(
    Arb.double().filter { it.isFinite() },
    Arb.bind(
      Arb.long(-MAX_DOUBLE_DIGITS..MAX_DOUBLE_DIGITS),
      Arb.int(0..MAX_DOUBLE_SCALE),
    ) { digits, scale -> digits / 10.0.pow(scale) },
  )

private fun finiteFloats(): Arb<Float> =
  Arb.choice(
    Arb.float().filter { it.isFinite() },
    Arb.bind(
      Arb.int(-MAX_FLOAT_DIGITS..MAX_FLOAT_DIGITS),
      Arb.int(0..MAX_FLOAT_SCALE),
    ) { digits, scale -> digits / 10.0f.pow(scale) },
  )

private fun String.withoutNegativeZero(): String =
  if (startsWith('-') && none { it in '1'..'9' }) {
    drop(1)
  } else {
    this
  }

// endregion
