package io.github.hhth24340005.math

/**
 * A non-negative distance in meters.
 *
 * Its values are always finite, never Infinity or NaN.
 * It is stored as a Float, so it keeps about 7 significant digits.
 *
 * For the smaller or larger of two, use [coerceAtMost] or [coerceAtLeast].
 * Unlike the generic `minOf` and `maxOf`, they never box.
 */
@JvmInline
public value class Distance private constructor(private val bits: Int) :
  Comparable<Distance> {
    /** This distance toward the right. */
    public val right: Horizontal get() = Horizontal.right(this)

    /** This distance toward the left. */
    public val left: Horizontal get() = Horizontal.left(this)

    /** This distance upward. */
    public val up: Vertical get() = Vertical.up(this)

    /** This distance downward. */
    public val down: Vertical get() = Vertical.down(this)

    public fun coerceAtMost(
      maximumValue: Distance,
    ): Distance =
      Distance(minBits(bits, maximumValue.bits))

    public fun coerceAtLeast(
      minimumValue: Distance,
    ): Distance =
      Distance(maxBits(bits, minimumValue.bits))

    /**
     * @throws IllegalArgumentException if [minimumValue] is greater than
     *   [maximumValue].
     */
    public fun coerceIn(
      minimumValue: Distance,
      maximumValue: Distance,
    ): Distance =
      Distance(clampBits(bits, minimumValue.bits, maximumValue.bits))

    /** Returns the distance in meters, such as `1.500 m`. */
    override fun toString(): String =
      toString(precision = 3)

    /**
     * Returns the distance in meters with [precision] decimal places,
     * such as `1.5 m`.
     *
     * @throws IllegalArgumentException if [precision] is negative.
     */
    public fun toString(
      @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
      precision: Int,
    ): String =
      "${meters.toString(precision = precision)} m"

    override fun compareTo(
      other: Distance,
    ): Int =
      meters.compareTo(other.meters)

    companion {
      /** No distance. */
      public val zero: Distance get() = Distance(ZERO_BITS)

      // region Double constructors

      /**
       * @throws IllegalArgumentException if [meters] is not within 0 to
       *   [Float.MAX_VALUE]. -0.0 counts as 0.
       */
      public fun fromMeters(
        meters: Double,
      ): Distance =
        Distance(meterBits(meters))

      /**
       * @throws IllegalArgumentException if [millimeters] is not within 0 to
       *   about 3.4e41. -0.0 counts as 0.
       */
      public fun fromMillimeters(
        millimeters: Double,
      ): Distance =
        Distance(millimeterBits(millimeters))

      /**
       * @throws IllegalArgumentException if [centimeters] is not within 0 to
       *   about 3.4e40. -0.0 counts as 0.
       */
      public fun fromCentimeters(
        centimeters: Double,
      ): Distance =
        Distance(centimeterBits(centimeters))

      /**
       * @throws IllegalArgumentException if [kilometers] is not within 0 to
       *   about 3.4e35. -0.0 counts as 0.
       */
      public fun fromKilometers(
        kilometers: Double,
      ): Distance =
        Distance(kilometerBits(kilometers))

      // endregion

      // region Float constructors

      /**
       * @throws IllegalArgumentException if [meters] is negative or not
       *   finite.
       */
      public fun fromMeters(
        meters: Float,
      ): Distance =
        fromMeters(meters.toDouble())

      /**
       * @throws IllegalArgumentException if [millimeters] is negative or not
       *   finite.
       */
      public fun fromMillimeters(
        millimeters: Float,
      ): Distance =
        fromMillimeters(millimeters.toDouble())

      /**
       * @throws IllegalArgumentException if [centimeters] is negative or not
       *   finite.
       */
      public fun fromCentimeters(
        centimeters: Float,
      ): Distance =
        fromCentimeters(centimeters.toDouble())

      /**
       * @throws IllegalArgumentException if [kilometers] is not within 0 to
       *   about 3.4e35. -0.0 counts as 0.
       */
      public fun fromKilometers(
        kilometers: Float,
      ): Distance =
        fromKilometers(kilometers.toDouble())

      // endregion

      // region Int constructors

      /** @throws IllegalArgumentException if [meters] is negative. */
      public fun fromMeters(
        meters: Int,
      ): Distance =
        fromMeters(meters.toDouble())

      /** @throws IllegalArgumentException if [millimeters] is negative. */
      public fun fromMillimeters(
        millimeters: Int,
      ): Distance =
        fromMillimeters(millimeters.toDouble())

      /** @throws IllegalArgumentException if [centimeters] is negative. */
      public fun fromCentimeters(
        centimeters: Int,
      ): Distance =
        fromCentimeters(centimeters.toDouble())

      /** @throws IllegalArgumentException if [kilometers] is negative. */
      public fun fromKilometers(
        kilometers: Int,
      ): Distance =
        fromKilometers(kilometers.toDouble())

      // endregion

      // region Long constructors

      /** @throws IllegalArgumentException if [meters] is negative. */
      public fun fromMeters(
        meters: Long,
      ): Distance =
        fromMeters(meters.toDouble())

      /** @throws IllegalArgumentException if [millimeters] is negative. */
      public fun fromMillimeters(
        millimeters: Long,
      ): Distance =
        fromMillimeters(millimeters.toDouble())

      /** @throws IllegalArgumentException if [centimeters] is negative. */
      public fun fromCentimeters(
        centimeters: Long,
      ): Distance =
        fromCentimeters(centimeters.toDouble())

      /** @throws IllegalArgumentException if [kilometers] is negative. */
      public fun fromKilometers(
        kilometers: Long,
      ): Distance =
        fromKilometers(kilometers.toDouble())

      // endregion
    }

    // region internal

    internal val meters: Float get() = Float.fromBits(bits)

    // endregion
  }

/**
 * A distance to the right or to the left, such as `3.meters.left`.
 *
 * Its values are always finite, never ±Infinity or NaN.
 * It is stored as a Float, so it keeps about 7 significant digits.
 */
@JvmInline
public value class Horizontal private constructor(private val bits: Int) {
  /** @throws ArithmeticException if the sum is beyond the Float range. */
  public operator fun plus(
    other: Horizontal,
  ): Horizontal =
    Horizontal(addBits(bits, other.bits))

  /**
   * @throws ArithmeticException if the difference is beyond the Float
   *   range.
   */
  public operator fun minus(
    other: Horizontal,
  ): Horizontal =
    Horizontal(subtractBits(bits, other.bits))

  /** Combines this with [other] into a displacement on the plane. */
  public operator fun plus(
    other: Vertical,
  ): Displacement2 =
    Displacement2(this, other)

  /** Combines this with the opposite of [other] on the plane. */
  public operator fun minus(
    other: Vertical,
  ): Displacement2 =
    Displacement2(this, -other)

  /** Returns the same distance toward the other side. */
  public operator fun unaryMinus(): Horizontal =
    Horizontal(negateBits(bits))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws IllegalArgumentException if [factor] is not finite.
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Double,
  ): Horizontal =
    Horizontal(timesBits(bits, factor))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws IllegalArgumentException if [divisor] is not finite.
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Double,
  ): Horizontal =
    Horizontal(divBits(bits, divisor))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws IllegalArgumentException if [factor] is not finite.
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Float,
  ): Horizontal =
    Horizontal(timesBits(bits, factor.toDouble()))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws IllegalArgumentException if [divisor] is not finite.
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Float,
  ): Horizontal =
    Horizontal(divBits(bits, divisor.toDouble()))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Int,
  ): Horizontal =
    Horizontal(timesBits(bits, factor.toDouble()))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Int,
  ): Horizontal =
    Horizontal(divBits(bits, divisor.toDouble()))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Long,
  ): Horizontal =
    Horizontal(timesBits(bits, factor.toDouble()))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Long,
  ): Horizontal =
    Horizontal(divBits(bits, divisor.toDouble()))

  /**
   * Returns the distance toward the right in meters, such as `→ -1.500 m`
   * for 1.5 meters to the left.
   */
  override fun toString(): String =
    toString(precision = 3)

  /**
   * Returns the distance toward the right in meters with [precision]
   * decimal places, such as `→ -1.5 m`.
   *
   * @throws IllegalArgumentException if [precision] is negative.
   */
  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    "→ ${rightMeters.toString(precision = precision)} m"

  companion {
    /** No distance to either side. */
    public val zero: Horizontal get() = Horizontal(ZERO_BITS)

    internal fun right(
      distance: Distance,
    ): Horizontal =
      Horizontal(distance.meters.toFiniteBitsOrThrow())

    internal fun left(
      distance: Distance,
    ): Horizontal =
      Horizontal((-distance.meters).toFiniteBitsOrThrow())

    internal fun fromRightMeters(
      meters: Float,
    ): Horizontal =
      Horizontal(meters.toFiniteBitsOrThrow())
  }

  // region internal

  internal val rightMeters: Float get() = Float.fromBits(bits)

  // endregion
}

/**
 * A distance upward or downward, such as `3.meters.down`.
 *
 * Its values are always finite, never ±Infinity or NaN.
 * It is stored as a Float, so it keeps about 7 significant digits.
 */
@JvmInline
public value class Vertical private constructor(private val bits: Int) {
  /** @throws ArithmeticException if the sum is beyond the Float range. */
  public operator fun plus(
    other: Vertical,
  ): Vertical =
    Vertical(addBits(bits, other.bits))

  /**
   * @throws ArithmeticException if the difference is beyond the Float
   *   range.
   */
  public operator fun minus(
    other: Vertical,
  ): Vertical =
    Vertical(subtractBits(bits, other.bits))

  /** Combines this with [other] into a displacement on the plane. */
  public operator fun plus(
    other: Horizontal,
  ): Displacement2 =
    Displacement2(other, this)

  /** Combines this with the opposite of [other] on the plane. */
  public operator fun minus(
    other: Horizontal,
  ): Displacement2 =
    Displacement2(-other, this)

  /** Returns the same distance toward the other side. */
  public operator fun unaryMinus(): Vertical =
    Vertical(negateBits(bits))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws IllegalArgumentException if [factor] is not finite.
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Double,
  ): Vertical =
    Vertical(timesBits(bits, factor))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws IllegalArgumentException if [divisor] is not finite.
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Double,
  ): Vertical =
    Vertical(divBits(bits, divisor))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws IllegalArgumentException if [factor] is not finite.
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Float,
  ): Vertical =
    Vertical(timesBits(bits, factor.toDouble()))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws IllegalArgumentException if [divisor] is not finite.
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Float,
  ): Vertical =
    Vertical(divBits(bits, divisor.toDouble()))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Int,
  ): Vertical =
    Vertical(timesBits(bits, factor.toDouble()))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Int,
  ): Vertical =
    Vertical(divBits(bits, divisor.toDouble()))

  /**
   * Goes toward the other side if [factor] is negative.
   *
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Long,
  ): Vertical =
    Vertical(timesBits(bits, factor.toDouble()))

  /**
   * Goes toward the other side if [divisor] is negative.
   *
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Long,
  ): Vertical =
    Vertical(divBits(bits, divisor.toDouble()))

  /**
   * Returns the distance upward in meters, such as `↑ -1.500 m` for
   * 1.5 meters downward.
   */
  override fun toString(): String =
    toString(precision = 3)

  /**
   * Returns the distance upward in meters with [precision] decimal places,
   * such as `↑ -1.5 m`.
   *
   * @throws IllegalArgumentException if [precision] is negative.
   */
  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    "↑ ${upMeters.toString(precision = precision)} m"

  companion {
    /** No distance upward or downward. */
    public val zero: Vertical get() = Vertical(ZERO_BITS)

    internal fun up(
      distance: Distance,
    ): Vertical =
      Vertical(distance.meters.toFiniteBitsOrThrow())

    internal fun down(
      distance: Distance,
    ): Vertical =
      Vertical((-distance.meters).toFiniteBitsOrThrow())

    internal fun fromUpMeters(
      meters: Float,
    ): Vertical =
      Vertical(meters.toFiniteBitsOrThrow())
  }

  // region internal

  internal val upMeters: Float get() = Float.fromBits(bits)

  // endregion
}

// region extension constructors

// region Double extensions

/**
 * @throws IllegalArgumentException if this is not within 0 to
 *   [Float.MAX_VALUE]. -0.0 counts as 0.
 */
public val Double.meters: Distance get() =
  Distance
    .fromMeters(this)

/**
 * @throws IllegalArgumentException if this is not within 0 to
 *   about 3.4e41. -0.0 counts as 0.
 */
public val Double.millimeters: Distance get() =
  Distance
    .fromMillimeters(this)

/**
 * @throws IllegalArgumentException if this is not within 0 to
 *   about 3.4e40. -0.0 counts as 0.
 */
public val Double.centimeters: Distance get() =
  Distance
    .fromCentimeters(this)

/**
 * @throws IllegalArgumentException if this is not within 0 to
 *   about 3.4e35. -0.0 counts as 0.
 */
public val Double.kilometers: Distance get() =
  Distance
    .fromKilometers(this)

// endregion

// region Float extensions

/**
 * @throws IllegalArgumentException if this is negative or not
 *   finite.
 */
public val Float.meters: Distance get() =
  Distance
    .fromMeters(this)

/**
 * @throws IllegalArgumentException if this is negative or not
 *   finite.
 */
public val Float.millimeters: Distance get() =
  Distance
    .fromMillimeters(this)

/**
 * @throws IllegalArgumentException if this is negative or not
 *   finite.
 */
public val Float.centimeters: Distance get() =
  Distance
    .fromCentimeters(this)

/**
 * @throws IllegalArgumentException if this is not within 0 to
 *   about 3.4e35. -0.0 counts as 0.
 */
public val Float.kilometers: Distance get() =
  Distance
    .fromKilometers(this)

// endregion

// region Int extensions

/** @throws IllegalArgumentException if this is negative. */
public val Int.meters: Distance get() =
  Distance
    .fromMeters(this)

/** @throws IllegalArgumentException if this is negative. */
public val Int.millimeters: Distance get() =
  Distance
    .fromMillimeters(this)

/** @throws IllegalArgumentException if this is negative. */
public val Int.centimeters: Distance get() =
  Distance
    .fromCentimeters(this)

/** @throws IllegalArgumentException if this is negative. */
public val Int.kilometers: Distance get() =
  Distance
    .fromKilometers(this)

// endregion

// region Long extensions

/** @throws IllegalArgumentException if this is negative. */
public val Long.meters: Distance get() =
  Distance
    .fromMeters(this)

/** @throws IllegalArgumentException if this is negative. */
public val Long.millimeters: Distance get() =
  Distance
    .fromMillimeters(this)

/** @throws IllegalArgumentException if this is negative. */
public val Long.centimeters: Distance get() =
  Distance
    .fromCentimeters(this)

/** @throws IllegalArgumentException if this is negative. */
public val Long.kilometers: Distance get() =
  Distance
    .fromKilometers(this)

// endregion

// endregion

// region boxing guards

@Deprecated(
  "Generic minOf boxes wherever this overload is not imported",
  ReplaceWith("a.coerceAtMost(b)"),
  DeprecationLevel.ERROR,
)
public fun minOf(
  a: Distance,
  b: Distance,
): Distance =
  a.coerceAtMost(b)

@Deprecated(
  "Generic maxOf boxes wherever this overload is not imported",
  ReplaceWith("a.coerceAtLeast(b)"),
  DeprecationLevel.ERROR,
)
public fun maxOf(
  a: Distance,
  b: Distance,
): Distance =
  a.coerceAtLeast(b)

// endregion

// region internal

// The bits of 0.0f, which are all clear
private const val ZERO_BITS = 0

private const val MILLIMETERS_PER_METER = 1000.0

private const val CENTIMETERS_PER_METER = 100.0

private const val METERS_PER_KILOMETER = 1000.0

private const val MAX_METERS = Float.MAX_VALUE.toDouble()

private const val MAX_MILLIMETERS = MAX_METERS * MILLIMETERS_PER_METER

private const val MAX_CENTIMETERS = MAX_METERS * CENTIMETERS_PER_METER

private const val MAX_KILOMETERS = MAX_METERS / METERS_PER_KILOMETER

private fun meterBits(
  meters: Double,
): Int {
  requireWithin(meters, MAX_METERS, "Meters")
  return meters.toFloat().toFiniteBitsOrThrow()
}

private fun millimeterBits(
  millimeters: Double,
): Int {
  requireWithin(millimeters, MAX_MILLIMETERS, "Millimeters")
  return (millimeters / MILLIMETERS_PER_METER).toFloat().toFiniteBitsOrThrow()
}

private fun centimeterBits(
  centimeters: Double,
): Int {
  requireWithin(centimeters, MAX_CENTIMETERS, "Centimeters")
  return (centimeters / CENTIMETERS_PER_METER).toFloat().toFiniteBitsOrThrow()
}

private fun kilometerBits(
  kilometers: Double,
): Int {
  requireWithin(kilometers, MAX_KILOMETERS, "Kilometers")
  return (kilometers * METERS_PER_KILOMETER).toFloat().toFiniteBitsOrThrow()
}

// NaN is not in any range, so this rejects it too
private fun requireWithin(
  value: Double,
  max: Double,
  unit: String,
) {
  require(value in 0.0..max) {
    "$unit must be within 0..$max, but got: $value"
  }
}

// Values are finite and never -0, so comparing as Floats is a total order

private fun minBits(
  a: Int,
  b: Int,
): Int =
  minOf(Float.fromBits(a), Float.fromBits(b)).toRawBits()

private fun maxBits(
  a: Int,
  b: Int,
): Int =
  maxOf(Float.fromBits(a), Float.fromBits(b)).toRawBits()

private fun clampBits(
  bits: Int,
  min: Int,
  max: Int,
): Int {
  require(Float.fromBits(min) <= Float.fromBits(max)) {
    "Minimum must not exceed maximum, but got: " +
      "${Float.fromBits(min)} > ${Float.fromBits(max)}"
  }
  return minBits(maxBits(bits, min), max)
}

private fun addBits(
  a: Int,
  b: Int,
): Int =
  (Float.fromBits(a) + Float.fromBits(b)).toFiniteBitsOrOverflow()

private fun subtractBits(
  a: Int,
  b: Int,
): Int =
  (Float.fromBits(a) - Float.fromBits(b)).toFiniteBitsOrOverflow()

private fun timesBits(
  bits: Int,
  factor: Double,
): Int {
  require(factor.isFinite()) { "Factor must be finite, but got: $factor" }
  return (Float.fromBits(bits) * factor).toFloat().toFiniteBitsOrOverflow()
}

private fun divBits(
  bits: Int,
  divisor: Double,
): Int {
  require(divisor.isFinite()) { "Divisor must be finite, but got: $divisor" }
  if (divisor == 0.0) {
    throw ArithmeticException("Division by zero")
  }
  return (Float.fromBits(bits) / divisor).toFloat().toFiniteBitsOrOverflow()
}

private fun negateBits(
  bits: Int,
): Int =
  (-Float.fromBits(bits)).toFiniteBitsOrThrow()

// endregion
