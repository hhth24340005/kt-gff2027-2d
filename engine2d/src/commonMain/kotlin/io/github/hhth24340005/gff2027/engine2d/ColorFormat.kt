package io.github.hhth24340005.gff2027.engine2d

import io.github.hhth24340005.gff2027.webgpu.TextureFormat

/** The format of the colors a [Pipeline] draws. */
public sealed interface ColorFormat {
  /** The format of the canvas, which its compiler applies when compiling. */
  public data object Canvas : ColorFormat

  public data object Rgba8Unorm : ColorFormat

  public data object Rgba8UnormSrgb : ColorFormat

  public data object Bgra8Unorm : ColorFormat

  public data object Bgra8UnormSrgb : ColorFormat
}

// region internal

internal fun ColorFormat.textureFormat(
  canvas: TextureFormat,
): TextureFormat =
  when (this) {
    Canvas -> canvas
    Rgba8Unorm -> RGBA8Unorm
    Rgba8UnormSrgb -> RGBA8UnormSrgb
    Bgra8Unorm -> BGRA8Unorm
    Bgra8UnormSrgb -> BGRA8UnormSrgb
  }

// endregion
