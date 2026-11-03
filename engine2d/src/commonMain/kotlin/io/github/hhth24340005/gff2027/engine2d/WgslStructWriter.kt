package io.github.hhth24340005.gff2027.engine2d

import io.github.hhth24340005.gff2027.math.Displacement2
import io.github.hhth24340005.gff2027.math.Distance
import io.github.hhth24340005.gff2027.math.HorizontalDistance
import io.github.hhth24340005.gff2027.math.HorizontalProportion
import io.github.hhth24340005.gff2027.math.Position2
import io.github.hhth24340005.gff2027.math.Proportion
import io.github.hhth24340005.gff2027.math.Proportion2
import io.github.hhth24340005.gff2027.math.VerticalDistance
import io.github.hhth24340005.gff2027.math.VerticalProportion

/**
 * Writes the members of a [WgslStruct] in order, aligned as WGSL lays out
 * a struct in storage. Each type becomes a WGSL type as follows.
 *
 * | Kotlin                 | WGSL    | Value                                 |
 * |------------------------|---------|---------------------------------------|
 * | [Distance]             | `f32`   | meters                                |
 * | [HorizontalDistance]   | `f32`   | meters toward the right               |
 * | [VerticalDistance]     | `f32`   | meters upward                         |
 * | [Displacement2]        | `vec2f` | meters toward the right and upward    |
 * | [Position2]            | `vec2f` | meters from the origin, as above      |
 * | [Proportion]           | `f32`   | 0 to 1                                |
 * | [HorizontalProportion] | `f32`   | 0 to 1 from the left                  |
 * | [VerticalProportion]   | `f32`   | 0 to 1 from the bottom                |
 * | [Proportion2]          | `vec2f` | 0 to 1 from the left and the bottom   |
 */
@Suppress("MethodOverloading")
public class WgslStructWriter internal constructor() {
  private var words = IntArray(INITIAL_CAPACITY)
  private var size = 0

  // The largest alignment of the members of the struct being written
  private var structAlignment = 1

  public fun write(
    value: Distance,
  ): Unit =
    writeF32(value.meters)

  public fun write(
    value: HorizontalDistance,
  ): Unit =
    writeF32(value.rightMeters)

  public fun write(
    value: VerticalDistance,
  ): Unit =
    writeF32(value.upMeters)

  public fun write(
    value: Displacement2,
  ): Unit =
    writeVec2f(value.horizontal.rightMeters, value.vertical.upMeters)

  public fun write(
    value: Position2,
  ): Unit =
    writePosition(value)

  public fun write(
    value: Proportion,
  ): Unit =
    writeF32(value.rate)

  public fun write(
    value: HorizontalProportion,
  ): Unit =
    writeF32(value.fromLeft.rate)

  public fun write(
    value: VerticalProportion,
  ): Unit =
    writeF32(value.fromBottom.rate)

  public fun write(
    value: Proportion2,
  ): Unit =
    writeVec2f(value.horizontal.fromLeft.rate, value.vertical.fromBottom.rate)

  // region internal

  /** Writes [structs] one after another, as an `array` of them. */
  internal fun writeAll(
    structs: List<WgslStruct>,
  ) {
    structs.forEach { struct ->
      structAlignment = 1
      struct.writeTo(this)
      // A struct's size is a multiple of its alignment
      alignTo(structAlignment)
    }
  }

  /** Returns what is written, as WGSL reads it in little-endian words. */
  internal fun toWords(): IntArray =
    words.copyOf(size)

  private fun writeF32(
    value: Float,
  ) {
    alignTo(1)
    append(value.toRawBits())
  }

  private fun writeVec2f(
    x: Float,
    y: Float,
  ) {
    alignTo(2)
    append(x.toRawBits())
    append(y.toRawBits())
  }

  private fun writePosition(
    position: Position2,
  ) {
    val fromOrigin = position - Position2.origin
    writeVec2f(fromOrigin.horizontal.rightMeters, fromOrigin.vertical.upMeters)
  }

  // Pads to a multiple of alignment words
  private fun alignTo(
    alignment: Int,
  ) {
    structAlignment = maxOf(structAlignment, alignment)
    while (size % alignment != 0) {
      append(0)
    }
  }

  private fun append(
    word: Int,
  ) {
    if (size == words.size) {
      words = words.copyOf(words.size * 2)
    }
    words[size] = word
    size++
  }

  // endregion
}

// region internal

/** Returns [structs] as WGSL reads an `array` of them. */
internal fun wgslWords(
  structs: List<WgslStruct>,
): IntArray {
  val writer = WgslStructWriter()
  writer.writeAll(structs)
  return writer.toWords()
}

private const val INITIAL_CAPACITY = 16

// endregion
