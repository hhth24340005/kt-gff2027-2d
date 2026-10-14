package io.github.hhth24340005.math

/**
 * Returns this value rounded half away from zero to [precision] decimal
 * places, such as `12.4` for `12.35.toString(precision = 1)`.
 *
 * The rounding works on the shortest decimal that reads back as this value,
 * so `1.15` rounds to `1.2` even though its binary value is slightly below.
 *
 * @throws IllegalArgumentException if [precision] is negative.
 */
internal fun Double.toString(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  precision: Int,
): String =
  formatDouble(this, precision)

/**
 * Returns this value rounded half away from zero to [precision] decimal
 * places, such as `12.4` for `12.35f.toString(precision = 1)`.
 *
 * The rounding works on the shortest decimal that reads back as this value,
 * so `1.15f` rounds to `1.2` even though its binary value is slightly below.
 *
 * @throws IllegalArgumentException if [precision] is negative.
 */
internal fun Float.toString(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  precision: Int,
): String =
  formatFloat(this, precision)

// region internal

private fun formatDouble(
  value: Double,
  precision: Int,
): String {
  requirePrecision(precision)
  if (!value.isFinite()) {
    return nonFiniteString(value.isNaN(), value > 0.0)
  }
  val bits = value.toRawBits()
  val biased = ((bits ushr DOUBLE_FRACTION_BITS) and DOUBLE_EXPONENT_MASK)
  val fraction = bits and ((1L shl DOUBLE_FRACTION_BITS) - 1)
  val digits =
    shortestDigits(
      significand = binarySignificand(biased, fraction, DOUBLE_FRACTION_BITS),
      exponent = binaryExponent(biased, DOUBLE_EXPONENT_OFFSET),
      lowerGapHalved = fraction == 0L && 1L < biased,
    )
  return roundedString(bits < 0L, digits, precision)
}

private fun formatFloat(
  value: Float,
  precision: Int,
): String {
  requirePrecision(precision)
  if (!value.isFinite()) {
    return nonFiniteString(value.isNaN(), value > 0.0f)
  }
  val bits = value.toRawBits()
  val biased = (bits ushr FLOAT_FRACTION_BITS).toLong() and FLOAT_EXPONENT_MASK
  val fraction = (bits and ((1 shl FLOAT_FRACTION_BITS) - 1)).toLong()
  val digits =
    shortestDigits(
      significand = binarySignificand(biased, fraction, FLOAT_FRACTION_BITS),
      exponent = binaryExponent(biased, FLOAT_EXPONENT_OFFSET),
      lowerGapHalved = fraction == 0L && 1L < biased,
    )
  return roundedString(bits < 0, digits, precision)
}

private fun requirePrecision(
  precision: Int,
) {
  require(0 <= precision) {
    "Precision must be non-negative, but got: $precision"
  }
}

private fun nonFiniteString(
  isNaN: Boolean,
  isPositive: Boolean,
): String =
  when {
    isNaN -> "NaN"
    isPositive -> "Infinity"
    else -> "-Infinity"
  }

private fun binarySignificand(
  biased: Long,
  fraction: Long,
  fractionBits: Int,
): Long =
  if (biased == 0L) {
    fraction
  } else {
    fraction or (1L shl fractionBits)
  }

// Subnormals share the exponent of the smallest normal numbers
private fun binaryExponent(
  biased: Long,
  offset: Int,
): Int =
  maxOf(biased.toInt(), 1) - offset

/** The decimal `0.d1 d2 ... dn × 10^point`, with no digits for zero. */
private class Digits(
  val digits: List<Int>,
  val point: Int,
)

/**
 * Finds the shortest digits that read back as `significand × 2^exponent`,
 * by Burger and Dybvig's free-format algorithm.
 *
 * Every number between the midpoints with the two neighbors reads back as
 * this value. The ratio `r / s` is the value, and `mPlus / s` and
 * `mMinus / s` are the distances to the upper and lower midpoints.
 */
private fun shortestDigits(
  significand: Long,
  exponent: Int,
  lowerGapHalved: Boolean,
): Digits {
  if (significand == 0L) {
    return Digits(emptyList(), 0)
  }
  // Ties read back as the even significand, so its midpoints are included
  val inclusive = significand % 2 == 0L
  val gapShift =
    if (lowerGapHalved) {
      1
    } else {
      0
    }
  var r: Natural
  var s: Natural
  var mPlus: Natural
  var mMinus: Natural
  if (0 <= exponent) {
    r = Natural.of(significand) shl (exponent + 1 + gapShift)
    s = Natural.of(2L shl gapShift)
    mPlus = Natural.of(1L) shl (exponent + gapShift)
    mMinus = Natural.of(1L) shl exponent
  } else {
    r = Natural.of(significand) shl (1 + gapShift)
    s = Natural.of(1L) shl (1 - exponent + gapShift)
    mPlus = Natural.of(1L shl gapShift)
    mMinus = Natural.of(1L)
  }

  // Scale so that the upper midpoint is within 0.1..1 of s
  var point = 0
  while (reachesUpper(r + mPlus, s, inclusive)) {
    s *= RADIX
    point++
  }
  while (!reachesUpper((r + mPlus) * RADIX, s, inclusive)) {
    r *= RADIX
    mPlus *= RADIX
    mMinus *= RADIX
    point--
  }

  val digits = mutableListOf<Int>()
  while (true) {
    r *= RADIX
    mPlus *= RADIX
    mMinus *= RADIX
    var digit = 0
    while (s <= r) {
      r -= s
      digit++
    }
    val lowOrder = r.compareTo(mMinus)
    val low = lowOrder < 0 || (inclusive && lowOrder == 0)
    val high = reachesUpper(r + mPlus, s, inclusive)
    if (!low && !high) {
      digits += digit
      continue
    }
    val roundsUp =
      when {
        !low -> true
        !high -> false
        else -> roundsUpAtLastDigit(r * 2, s, digit)
      }
    digits +=
      if (roundsUp) {
        digit + 1
      } else {
        digit
      }
    return Digits(digits, point)
  }
}

private fun reachesUpper(
  upper: Natural,
  s: Natural,
  inclusive: Boolean,
): Boolean =
  s.compareTo(upper).let { it < 0 || (inclusive && it == 0) }

// Picks the nearer last digit, or the even one on a tie
private fun roundsUpAtLastDigit(
  twiceRest: Natural,
  s: Natural,
  digit: Int,
): Boolean {
  val order = twiceRest.compareTo(s)
  return 0 < order || (order == 0 && digit % 2 == 1)
}

private fun roundedString(
  negative: Boolean,
  decimal: Digits,
  precision: Int,
): String {
  var point = decimal.point
  val keep = point + precision
  val kept =
    if (decimal.digits.size <= keep) {
      decimal.digits.toMutableList()
    } else {
      decimal.digits
        .take(maxOf(keep, 0))
        .toMutableList()
    }
  if (0 <= keep &&
    keep < decimal.digits.size &&
    HALF_DIGIT <= decimal.digits[keep]
  ) {
    if (incrementOverflows(kept)) {
      kept.add(0, 1)
      point++
    }
  }
  return buildString {
    if (negative && kept.any { it != 0 }) {
      append('-')
    }
    if (point <= 0) {
      append('0')
    }
    for (i in 0 until point) {
      append(kept.getOrElse(i) { 0 })
    }
    if (0 < precision) {
      append('.')
    }
    for (i in point until point + precision) {
      append(kept.getOrElse(i) { 0 })
    }
  }
}

// Adds one to the last digit and returns whether a carry is left over
private fun incrementOverflows(
  digits: MutableList<Int>,
): Boolean {
  for (i in digits.indices.reversed()) {
    if (digits[i] < RADIX - 1) {
      digits[i]++
      return false
    }
    digits[i] = 0
  }
  return true
}

/** A non-negative integer of any size, in 32-bit limbs from the lowest. */
private class Natural(limbs: IntArray) : Comparable<Natural> {
  private val limbs: IntArray = limbs.copyOf(limbs.indexOfLast { it != 0 } + 1)

  operator fun plus(
    other: Natural,
  ): Natural {
    val size = maxOf(limbs.size, other.limbs.size)
    val result = IntArray(size + 1)
    var carry = 0L
    for (i in 0 until size) {
      val sum = limb(i) + other.limb(i) + carry
      result[i] = sum.toInt()
      carry = sum ushr LIMB_BITS
    }
    result[size] = carry.toInt()
    return Natural(result)
  }

  // The caller makes sure that other is not greater
  operator fun minus(
    other: Natural,
  ): Natural {
    val result = IntArray(limbs.size)
    var borrow = 0L
    for (i in limbs.indices) {
      val difference = limb(i) - other.limb(i) - borrow
      result[i] = difference.toInt()
      // The sign bit is set exactly when this limb borrows
      borrow = difference ushr (Long.SIZE_BITS - 1)
    }
    return Natural(result)
  }

  operator fun times(
    factor: Int,
  ): Natural {
    val result = IntArray(limbs.size + 1)
    var carry = 0L
    for (i in limbs.indices) {
      val product = limb(i) * factor + carry
      result[i] = product.toInt()
      carry = product ushr LIMB_BITS
    }
    result[limbs.size] = carry.toInt()
    return Natural(result)
  }

  infix fun shl(
    bits: Int,
  ): Natural {
    val words = bits / LIMB_BITS
    val shift = bits % LIMB_BITS
    val result = IntArray(limbs.size + words + 1)
    for (i in limbs.indices) {
      val shifted = limb(i) shl shift
      result[i + words] = result[i + words] or shifted.toInt()
      result[i + words + 1] = (shifted ushr LIMB_BITS).toInt()
    }
    return Natural(result)
  }

  override fun compareTo(
    other: Natural,
  ): Int {
    if (limbs.size != other.limbs.size) {
      return limbs.size.compareTo(other.limbs.size)
    }
    for (i in limbs.indices.reversed()) {
      val order = limb(i).compareTo(other.limb(i))
      if (order != 0) {
        return order
      }
    }
    return 0
  }

  private fun limb(
    index: Int,
  ): Long =
    limbs.getOrElse(index) { 0 }.toLong() and LIMB_MASK

  companion object {
    fun of(
      value: Long,
    ): Natural =
      Natural(intArrayOf(value.toInt(), (value ushr LIMB_BITS).toInt()))
  }
}

private const val RADIX = 10

private const val HALF_DIGIT = 5

private const val LIMB_BITS = 32

private const val LIMB_MASK = 0xFFFF_FFFFL

private const val DOUBLE_FRACTION_BITS = 52

private const val DOUBLE_EXPONENT_MASK = 0x7FFL

// The exponent bias plus the fraction bits
private const val DOUBLE_EXPONENT_OFFSET = 1075

private const val FLOAT_FRACTION_BITS = 23

private const val FLOAT_EXPONENT_MASK = 0xFFL

private const val FLOAT_EXPONENT_OFFSET = 150

// endregion
