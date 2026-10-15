package io.github.hhth24340005.gff2027.math

import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * A position within a cycle, from 0 inclusive to 1 exclusive.
 *
 * It splits a cycle into 2^32 equal steps, so every position has the same
 * resolution and adding phases wraps around exactly.
 */
@JvmInline
public value class Phase private constructor(private val bits: Int) {
  /** Wraps around past the end of the cycle. */
  public operator fun plus(
    other: Phase,
  ): Phase =
    Phase(addBits(bits, other.bits))

  /** Wraps around past the start of the cycle. */
  public operator fun minus(
    other: Phase,
  ): Phase =
    Phase(subtractBits(bits, other.bits))

  /** Returns the phase as far before the start as this is after it. */
  public operator fun unaryMinus(): Phase =
    Phase(negateBits(bits))

  /** Adds this [times] times, going backward if [times] is negative. */
  public operator fun times(
    times: Int,
  ): Phase =
    Phase(multiplyBits(bits, times))

  /** Returns the position in cycles, such as `Phase(0.250)`. */
  override fun toString(): String =
    toString(precision = 3)

  /**
   * Returns the position in cycles with [precision] decimal places,
   * such as `Phase(0.25)`.
   *
   * @throws IllegalArgumentException if [precision] is negative.
   */
  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    "Phase(${phaseString(toDouble(), precision)})"

  companion {
    /** The start of a cycle. */
    public val zero: Phase get() = Phase(ALL_ZERO_BITS)

    /**
     * Keeps only the fractional part of [cycles], so `1.25` and `-0.75`
     * are both a quarter cycle.
     *
     * @throws IllegalArgumentException if [cycles] is not finite.
     */
    public fun fromCycles(
      cycles: Double,
    ): Phase =
      Phase(phaseBits(cycles))
  }

  // region internal

  /** The position in cycles, from 0 inclusive to 1 exclusive. */
  internal fun toDouble(): Double =
    phaseToDouble(bits)

  // endregion
}

// region internal

private const val ALL_ZERO_BITS = 0

private const val POW_2_32 = (1L shl Int.SIZE_BITS).toDouble()

private fun phaseBits(
  cycles: Double,
): Int {
  require(cycles.isFinite()) { "Cycles must be finite, but got: $cycles" }
  val fraction = cycles - floor(cycles)
  return (fraction * POW_2_32).roundToLong().toInt()
}

private fun phaseString(
  cycles: Double,
  precision: Int,
): String {
  val rounded = cycles.toString(precision = precision)
  // Rounding never goes past one cycle, so reaching it means a full cycle
  return if (rounded == 1.0.toString(precision = precision)) {
    0.0.toString(precision = precision)
  } else {
    rounded
  }
}

private fun phaseToDouble(
  bits: Int,
): Double =
  bits.toUInt().toDouble() / POW_2_32

// Int arithmetic wraps around at 2^32, which is exactly one cycle

private fun addBits(
  a: Int,
  b: Int,
): Int =
  a + b

private fun subtractBits(
  a: Int,
  b: Int,
): Int =
  a - b

private fun negateBits(
  bits: Int,
): Int =
  -bits

private fun multiplyBits(
  bits: Int,
  times: Int,
): Int =
  bits * times

// endregion
