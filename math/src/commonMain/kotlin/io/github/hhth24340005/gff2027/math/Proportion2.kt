package io.github.hhth24340005.gff2027.math

/**
 * A point in the unit square, such as
 * `Proportion2(25.percent.fromLeft, 10.percent.fromTop)`.
 */
@JvmInline
public value class Proportion2 private constructor(private val packed: Long) {
  public constructor(
    horizontal: HorizontalProportion,
    vertical: VerticalProportion,
  ) : this(
    pack(horizontal.fromLeft.unormBits, vertical.fromBottom.unormBits),
  )

  public val horizontal: HorizontalProportion
    get() = Proportion.fromUnormBits(unpackHigh(packed)).fromLeft

  public val vertical: VerticalProportion
    get() = Proportion.fromUnormBits(unpackLow(packed)).fromBottom

  /** Returns both components, such as `(→ 25.0%, ↑ 75.0%)`. */
  override fun toString(): String =
    toString(precision = 1)

  /**
   * Returns both components with [precision] decimal places, such as
   * `(→ 25%, ↑ 75%)`.
   *
   * @throws IllegalArgumentException if [precision] is negative.
   */
  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    proportionString(horizontal, vertical, precision)
}

// region internal

private fun proportionString(
  horizontal: HorizontalProportion,
  vertical: VerticalProportion,
  precision: Int,
): String {
  val h = horizontal.toString(precision = precision)
  val v = vertical.toString(precision = precision)
  return "($h, $v)"
}

private fun pack(
  high: Int,
  low: Int,
): Long =
  high.toLong() shl Int.SIZE_BITS or (low.toLong() and LOW_MASK)

private fun unpackHigh(
  packed: Long,
): Int =
  (packed ushr Int.SIZE_BITS).toInt()

private fun unpackLow(
  packed: Long,
): Int =
  packed.toInt()

private const val LOW_MASK = 0xFFFF_FFFFL

// endregion
