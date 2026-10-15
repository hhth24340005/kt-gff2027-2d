package io.github.hhth24340005.gff2027.math

import kotlin.math.roundToLong

/**
 * A part of a whole, from 0 to 1 inclusive.
 *
 * It splits the whole into 2^32 - 1 equal steps, like a GPU's unorm, so both
 * 0 and 1 are exact.
 *
 * For the smaller or larger of two, use [coerceAtMost] or [coerceAtLeast].
 * Unlike the generic `minOf` and `maxOf`, they never box.
 */
@JvmInline
public value class Proportion private constructor(private val bits: Int) :
  Comparable<Proportion> {
    /** 1 minus this, which is exact. */
    public val complement: Proportion get() = Proportion(complementBits(bits))

    public operator fun times(
      other: Proportion,
    ): Proportion =
      Proportion(multiplyBits(bits, other.bits))

    /**
     * Returns the point [t] of the way from this to [to], so `zero` gives
     * this and `one` gives [to].
     */
    public fun lerp(
      to: Proportion,
      t: Proportion,
    ): Proportion =
      Proportion(lerpBits(bits, to.bits, t.bits))

    public fun coerceAtMost(
      maximumValue: Proportion,
    ): Proportion =
      Proportion(minBits(bits, maximumValue.bits))

    public fun coerceAtLeast(
      minimumValue: Proportion,
    ): Proportion =
      Proportion(maxBits(bits, minimumValue.bits))

    /**
     * @throws IllegalArgumentException if [minimumValue] is greater than
     *   [maximumValue].
     */
    public fun coerceIn(
      minimumValue: Proportion,
      maximumValue: Proportion,
    ): Proportion =
      Proportion(clampBits(bits, minimumValue.bits, maximumValue.bits))

    override fun compareTo(
      other: Proportion,
    ): Int =
      bits.toUInt().compareTo(other.bits.toUInt())

    /** Returns the proportion in percent, such as `25.0%`. */
    override fun toString(): String =
      toString(precision = 1)

    /**
     * Returns the proportion in percent with [precision] decimal places,
     * such as `25%`.
     *
     * @throws IllegalArgumentException if [precision] is negative.
     */
    public fun toString(
      @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
      precision: Int,
    ): String =
      "${(toDouble() * PERCENT).toString(precision = precision)}%"

    companion {
      /** None of the whole. */
      public val zero: Proportion get() = Proportion(ZERO_ALL_BITS)

      /** All of the whole. */
      public val one: Proportion get() = Proportion(ONE_ALL_BITS)

      /**
       * @throws IllegalArgumentException if [value] is not within 0..1.
       *   -0.0 counts as 0.
       */
      public fun fromRateOrThrow(
        value: Double,
      ): Proportion =
        Proportion(proportionBitsOrThrow(value))
    }

    // region internal

    internal fun toDouble(): Double =
      proportionToDouble(bits)

    // endregion
  }

// region extension constructors

public val Double.percent: Proportion get() =
  Proportion.fromRateOrThrow(this / 100)

public val Float.percent: Proportion get() =
  toDouble().percent

public val Int.percent: Proportion get() =
  toDouble().percent

public val Long.percent: Proportion get() =
  toDouble().percent

// endregion

// region boxing guards

@Deprecated(
  "Generic minOf boxes wherever this overload is not imported",
  ReplaceWith("a.coerceAtMost(b)"),
  DeprecationLevel.ERROR,
)
public fun minOf(
  a: Proportion,
  b: Proportion,
): Proportion =
  a.coerceAtMost(b)

@Deprecated(
  "Generic maxOf boxes wherever this overload is not imported",
  ReplaceWith("a.coerceAtLeast(b)"),
  DeprecationLevel.ERROR,
)
public fun maxOf(
  a: Proportion,
  b: Proportion,
): Proportion =
  a.coerceAtLeast(b)

// endregion

// region internal

private const val ZERO_ALL_BITS = 0

private const val ONE_ALL_BITS = -1

private const val STEPS = UInt.MAX_VALUE.toDouble()

private const val PERCENT = 100.0

private fun proportionBitsOrThrow(
  value: Double,
): Int {
  require(value in 0.0..1.0) { "Value must be within 0..1, but got: $value" }
  return (value * STEPS).roundToLong().toInt()
}

// 1 / (2^32 - 1)
// = lim(n -> ∞) Σ(k = 1, n) 2^((-32) * k)
// = 2^-32 + 2^-64 + 2^-96 + ...
// But a Double keeps only the first two terms
private const val POW_2_32 = (1L shl Int.SIZE_BITS).toDouble()
private const val STEP = (1.0 / POW_2_32) + (1.0 / POW_2_32 / POW_2_32)

private fun proportionToDouble(
  bits: Int,
): Double =
  bits.toUInt().toDouble() * STEP

private fun multiplyBits(
  a: Int,
  b: Int,
): Int =
  divideByOneRounded(a.toUInt().toULong() * b.toUInt().toULong())

private fun complementBits(
  bits: Int,
): Int =
  bits.inv()

// a + (b - a) * t
// = (a * 1) - (a * t) + (b * t)
// = a * (1 - t) + b * t
private fun lerpBits(
  from: Int,
  to: Int,
  t: Int,
): Int =
  lerpBitsULong(
    from.toUInt().toULong(),
    to.toUInt().toULong(),
    t.toUInt().toULong(),
  )

private fun lerpBitsULong(
  from: ULong,
  to: ULong,
  t: ULong,
): Int {
  val one = UInt.MAX_VALUE.toULong()
  val sum = (from * (one - t)) + (to * t)
  return divideByOneRounded(sum)
}

private fun divideByOneRounded(
  scaled: ULong,
): Int {
  val one = UInt.MAX_VALUE.toULong()

  // never overflows, we're just going to add 2^31 order to (2^64 - 2^33 + 1)
  // later on we're even going to add 2^32 order but still no worry
  val x = scaled + (one / 2uL)

  // x / (2^32 - 1)
  // = x * Σ(k = 1, ∞) 2^(-32 * k)
  // ≈ x * (2^-32 + 2^-64) + 2^-32
  // = (x + 1 + x / 2^32) / 2^32
  // Both floor to the same integer if x in 0..<(2^32 + 1)(2^32 - 1)
  val numerator = x + 1uL + (x shr UInt.SIZE_BITS)
  return (numerator shr UInt.SIZE_BITS).toInt()
}

private fun minBits(
  a: Int,
  b: Int,
): Int =
  minOf(a.toUInt(), b.toUInt()).toInt()

private fun maxBits(
  a: Int,
  b: Int,
): Int =
  maxOf(a.toUInt(), b.toUInt()).toInt()

private fun clampBits(
  bits: Int,
  min: Int,
  max: Int,
): Int {
  require(min.toUInt() <= max.toUInt()) {
    "Minimum must not exceed maximum, but got: " +
      "${proportionToDouble(min)} > ${proportionToDouble(max)}"
  }
  return minBits(maxBits(bits, min), max)
}

// endregion
