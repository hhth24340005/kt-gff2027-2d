package io.github.hhth24340005.gff2027.engine2d

/** A value that shaders read. Bind its type with `bind`. */
public sealed interface ShaderResource

/**
 * A WGSL struct that shaders read as `var<storage, read>`. [writeTo] writes
 * its members in the order the WGSL struct declares them.
 */
public interface WgslStruct : ShaderResource {
  public fun writeTo(
    writer: WgslStructWriter,
  )
}

/**
 * [elements] that shaders read as `var<storage, read>` of `array<T>`. WGSL
 * cannot bind an empty array, so it needs at least one element.
 */
public class WgslArray<T : WgslStruct>(public val elements: List<T>) :
  ShaderResource {
  init {
    require(elements.isNotEmpty()) { "WGSL cannot bind an empty array" }
  }
}

/**
 * An image that shaders sample as `texture_2d<f32>`, of [size] given as width
 * to height. [filter] picks colors between texels.
 */
public sealed interface Image : ShaderResource {
  public val size: Pair<Int, Int>
  public val filter: ImageFilter
}

/** An image whose texels are ARGB ints in sRGB, top row first. */
public fun Image(
  size: Pair<Int, Int>,
  argb: IntArray,
  filter: ImageFilter = Nearest,
): Image =
  Pixels(size, argb, filter)

/** How an [Image] picks colors between texels. */
public enum class ImageFilter {
  /** Takes the closest texel, which keeps pixel art sharp. */
  Nearest,

  /** Blends the surrounding texels. */
  Linear,
}

// region internal

internal class Pixels(
  override val size: Pair<Int, Int>,
  val argb: IntArray,
  override val filter: ImageFilter,
) : Image

// endregion
