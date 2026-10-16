package io.github.hhth24340005.math

import kotlin.math.PI

@JvmInline
public value class Rotation private constructor(private val ccw: Angle) {
  public operator fun plus(
    other: Rotation,
  ): Rotation =
    Rotation(ccw + other.ccw)

  public operator fun minus(
    other: Rotation,
  ): Rotation =
    Rotation(ccw - other.ccw)

  public operator fun times(
    other: Int,
  ): Rotation =
    Rotation(ccw * other)

  public operator fun unaryMinus(): Rotation =
    Rotation(-ccw)

  override fun toString(): String =
    toString(precision = 1)

  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    rotationString(ccw, precision = precision)

  companion {
    /** No rotation. */
    public val zero: Rotation get() = Rotation(Angle.zero)

    /** Rotation of 180 degrees, or π radians. */
    public val opposite: Rotation get() = Rotation(Angle.opposite)

    internal fun ccw(
      angle: Angle,
    ): Rotation =
      Rotation(angle)

    internal fun cw(
      angle: Angle,
    ): Rotation =
      Rotation(-angle)
  }
}

@JvmInline
public value class Angle private constructor(private val progression: Phase) {
  public val ccw: Rotation get() = Rotation.ccw(this)

  public val cw: Rotation get() = Rotation.cw(this)

  public operator fun plus(
    other: Angle,
  ): Angle =
    Angle(this.progression + other.progression)

  public operator fun minus(
    other: Angle,
  ): Angle =
    Angle(this.progression - other.progression)

  public operator fun times(
    other: Int,
  ): Angle =
    Angle(this.progression * other)

  public operator fun unaryMinus(): Angle =
    Angle(-progression)

  override fun toString(): String =
    toString(precision = 1)

  public fun toString(
    @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
    precision: Int,
  ): String =
    "${(signedTurns * 360).toString(precision = precision)}°"

  companion {
    public val zero: Angle get() = Angle(Phase.zero)

    /** Opposite direction; 180° or π radians */
    public val opposite: Angle get() = Angle(Phase.fromCycles(0.5))

    /** @throws IllegalArgumentException if [turns] is not finite. */
    public fun fromTurns(
      turns: Double,
    ): Angle =
      Angle(Phase.fromCycles(turns))

    /** @throws IllegalArgumentException if [radians] is not finite. */
    public fun fromRadians(
      radians: Double,
    ): Angle =
      Angle(Phase.fromCycles(radians % (2 * PI) / (2 * PI)))

    /** @throws IllegalArgumentException if [degrees] is not finite. */
    public fun fromDegrees(
      degrees: Double,
    ): Angle =
      Angle(Phase.fromCycles(degrees % 360.0 / 360.0))
  }

  // region internal

  @PublishedApi
  internal val signedTurns: Double
    get() {
      val turns = progression.toDouble()
      return if (turns <= 0.5) {
        turns
      } else {
        turns - 1.0
      }
    }

  // endregion
}

@JvmInline
public value class Direction2 private constructor(
  private val fromRight: Rotation,
) {
  public operator fun plus(
    other: Rotation,
  ): Direction2 =
    Direction2(fromRight + other)

  public operator fun minus(
    other: Rotation,
  ): Direction2 =
    Direction2(fromRight - other)

  public operator fun minus(
    other: Direction2,
  ): Rotation =
    fromRight - other.fromRight

  companion {
    public val up: Direction2 get() = Direction2(0.25.turns.ccw)

    public val right: Direction2 get() = Direction2(Rotation.zero)

    public val down: Direction2 get() = Direction2(0.25.turns.cw)

    public val left: Direction2 get() = Direction2(Rotation.opposite)
  }
}

// region extension constructors

/** @throws IllegalArgumentException if [this] is not finite. */
public val Double.turns: Angle get() = Angle.fromTurns(this)

/** @throws IllegalArgumentException if [this] is not finite. */
public val Double.radians: Angle get() = Angle.fromRadians(this)

/** @throws IllegalArgumentException if [this] is not finite. */
public val Double.degrees: Angle get() = Angle.fromDegrees(this)

// endregion

// region internal

private inline fun <R> smallerMagnitude(
  ccwAngle: Angle,
  ccw: (Angle) -> R,
  cw: (Angle) -> R,
  none: () -> R,
  opposite: () -> R,
): R {
  val turns = ccwAngle.signedTurns
  return if (turns == 0.0) {
    none()
  } else if (turns == 0.5) {
    opposite()
  } else if (0.0 < turns) {
    ccw(ccwAngle)
  } else {
    cw(-ccwAngle)
  }
}

private fun rotationString(
  ccwAngle: Angle,
  precision: Int,
): String =
  smallerMagnitude(
    ccwAngle,
    ccw = { "${it.toString(precision = precision)} CCW" },
    cw = { "${it.toString(precision = precision)} CW" },
    none = { Angle.zero.toString(precision = precision) },
    opposite = { Angle.opposite.toString(precision = precision) },
  )

// endregion
