package io.github.hhth24340005.math

/**
 * A move on the plane, such as `3.meters.right + 1.meters.down`.
 *
 * Its values are always finite, never ±Infinity or NaN.
 */
@JvmInline
public value class Displacement2 private constructor(
  private val packed: Long,
) {
  internal constructor(
    horizontal: Horizontal,
    vertical: Vertical,
  ) : this(pack(horizontal.rightMeters, vertical.upMeters))

  public val horizontal: Horizontal
    get() = Horizontal.fromRightMeters(unpackHigh(packed))

  public val vertical: Vertical
    get() = Vertical.fromUpMeters(unpackLow(packed))

  /** @throws ArithmeticException if the sum is beyond the Float range. */
  public operator fun plus(
    other: Displacement2,
  ): Displacement2 =
    Displacement2(horizontal + other.horizontal, vertical + other.vertical)

  /**
   * @throws ArithmeticException if the difference is beyond the Float
   *   range.
   */
  public operator fun minus(
    other: Displacement2,
  ): Displacement2 =
    Displacement2(horizontal - other.horizontal, vertical - other.vertical)

  /** @throws ArithmeticException if the sum is beyond the Float range. */
  public operator fun plus(
    other: Horizontal,
  ): Displacement2 =
    Displacement2(horizontal + other, vertical)

  /**
   * @throws ArithmeticException if the difference is beyond the Float
   *   range.
   */
  public operator fun minus(
    other: Horizontal,
  ): Displacement2 =
    Displacement2(horizontal - other, vertical)

  /** @throws ArithmeticException if the sum is beyond the Float range. */
  public operator fun plus(
    other: Vertical,
  ): Displacement2 =
    Displacement2(horizontal, vertical + other)

  /**
   * @throws ArithmeticException if the difference is beyond the Float
   *   range.
   */
  public operator fun minus(
    other: Vertical,
  ): Displacement2 =
    Displacement2(horizontal, vertical - other)

  /** Returns the same move toward the opposite direction. */
  public operator fun unaryMinus(): Displacement2 =
    Displacement2(-horizontal, -vertical)

  /**
   * Goes toward the opposite direction if [factor] is negative.
   *
   * @throws IllegalArgumentException if [factor] is not finite.
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Double,
  ): Displacement2 =
    Displacement2(horizontal * factor, vertical * factor)

  /**
   * Goes toward the opposite direction if [divisor] is negative.
   *
   * @throws IllegalArgumentException if [divisor] is not finite.
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Double,
  ): Displacement2 =
    Displacement2(horizontal / divisor, vertical / divisor)

  /**
   * Goes toward the opposite direction if [factor] is negative.
   *
   * @throws IllegalArgumentException if [factor] is not finite.
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Float,
  ): Displacement2 =
    Displacement2(horizontal * factor, vertical * factor)

  /**
   * Goes toward the opposite direction if [divisor] is negative.
   *
   * @throws IllegalArgumentException if [divisor] is not finite.
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Float,
  ): Displacement2 =
    Displacement2(horizontal / divisor, vertical / divisor)

  /**
   * Goes toward the opposite direction if [factor] is negative.
   *
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Int,
  ): Displacement2 =
    Displacement2(horizontal * factor, vertical * factor)

  /**
   * Goes toward the opposite direction if [divisor] is negative.
   *
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Int,
  ): Displacement2 =
    Displacement2(horizontal / divisor, vertical / divisor)

  /**
   * Goes toward the opposite direction if [factor] is negative.
   *
   * @throws ArithmeticException if the product is beyond the Float range.
   */
  public operator fun times(
    factor: Long,
  ): Displacement2 =
    Displacement2(horizontal * factor, vertical * factor)

  /**
   * Goes toward the opposite direction if [divisor] is negative.
   *
   * @throws ArithmeticException if [divisor] is zero, or the quotient is
   *   beyond the Float range.
   */
  public operator fun div(
    divisor: Long,
  ): Displacement2 =
    Displacement2(horizontal / divisor, vertical / divisor)

  /** Returns both components, such as `(→ 3.000 m, ↑ -1.000 m)`. */
  override fun toString(): String =
    toString(precision = 3)

  /**
   * Returns both components with [precision] decimal places, such as
   * `(→ 3.0 m, ↑ -1.0 m)`.
   *
   * @throws IllegalArgumentException if [precision] is negative.
   */
  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String {
    val h = horizontal.toString(precision = precision)
    val v = vertical.toString(precision = precision)
    return "($h, $v)"
  }

  companion {
    /** No move. */
    public val zero: Displacement2
      get() = Displacement2(Horizontal.zero, Vertical.zero)
  }
}

/**
 * A point on the plane.
 *
 * It has no constructor; a point comes from another point and a move,
 * such as `player + 2.meters.up`.
 */
@JvmInline
public value class Position2 private constructor(
  private val fromOrigin: Displacement2,
) {
  /** @throws ArithmeticException if the result is beyond the Float range. */
  public operator fun plus(
    other: Displacement2,
  ): Position2 =
    Position2(fromOrigin + other)

  /** @throws ArithmeticException if the result is beyond the Float range. */
  public operator fun minus(
    other: Displacement2,
  ): Position2 =
    Position2(fromOrigin - other)

  /** @throws ArithmeticException if the result is beyond the Float range. */
  public operator fun plus(
    other: Horizontal,
  ): Position2 =
    Position2(fromOrigin + other)

  /** @throws ArithmeticException if the result is beyond the Float range. */
  public operator fun minus(
    other: Horizontal,
  ): Position2 =
    Position2(fromOrigin - other)

  /** @throws ArithmeticException if the result is beyond the Float range. */
  public operator fun plus(
    other: Vertical,
  ): Position2 =
    Position2(fromOrigin + other)

  /** @throws ArithmeticException if the result is beyond the Float range. */
  public operator fun minus(
    other: Vertical,
  ): Position2 =
    Position2(fromOrigin - other)

  /**
   * Returns the move from [other] to this.
   *
   * @throws ArithmeticException if the move is beyond the Float range.
   */
  public operator fun minus(
    other: Position2,
  ): Displacement2 =
    fromOrigin - other.fromOrigin

  /**
   * Returns the move from the origin, such as
   * `origin + (→ 3.000 m, ↑ 0.000 m)`.
   */
  override fun toString(): String =
    toString(precision = 3)

  /**
   * Returns the move from the origin with [precision] decimal places,
   * such as `origin + (→ 3.0 m, ↑ 0.0 m)`.
   *
   * @throws IllegalArgumentException if [precision] is negative.
   */
  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    "origin + ${fromOrigin.toString(precision = precision)}"

  companion {
    /** The point that every other point is measured from. */
    @OriginApi
    public val origin: Position2 get() = Position2(Displacement2.zero)
  }
}

// region internal

private fun pack(
  high: Float,
  low: Float,
): Long =
  high.toRawBits().toLong() shl Int.SIZE_BITS or
    (low.toRawBits().toLong() and LOW_MASK)

private fun unpackHigh(
  packed: Long,
): Float =
  Float.fromBits((packed ushr Int.SIZE_BITS).toInt())

private fun unpackLow(
  packed: Long,
): Float =
  Float.fromBits(packed.toInt())

private const val LOW_MASK = 0xFFFF_FFFFL

// endregion
