package io.github.hhth24340005.gff2027.webgpu

/**
 * A target that presents rendered images to a window. Obtained from
 * `Instance.createSurface`, which each platform defines for its windows.
 */
public expect class Surface

/**
 * Lists the formats [adapter] can present to this surface, preferred first.
 */
public fun Surface.supportedFormats(
  adapter: Adapter,
): List<TextureFormat> =
  supportedFormatsImpl(adapter)

/**
 * Prepares this surface to present images of [format] and [size], given as
 * width to height, rendered by [device]. Call it again when the window is
 * resized.
 */
public fun Surface.configure(
  device: Device,
  format: TextureFormat,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  size: Pair<Int, Int>,
) {
  val [width, height] = size
  configureImpl(device, format, width, height)
}

// region internal

internal expect fun Surface.supportedFormatsImpl(
  adapter: Adapter,
): List<TextureFormat>

internal expect fun Surface.configureImpl(
  device: Device,
  format: TextureFormat,
  width: Int,
  height: Int,
)

internal expect fun Surface.release()

// endregion
