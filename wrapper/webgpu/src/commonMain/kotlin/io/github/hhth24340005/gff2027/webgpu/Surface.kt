package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

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

/**
 * Returns the texture to draw the next frame to, or `null` when the frame
 * should be skipped, for example while the window is hidden. Fails when the
 * surface needs [configure] again.
 */
context(scope: ResourceScope)
public suspend fun Surface.getCurrentTexture(): Texture? =
  scope.install(
    acquire = { getCurrentTextureImpl() },
    release = { texture, _ -> texture?.release() },
  )

/** Shows the texture from [getCurrentTexture] on the window. */
public fun Surface.present(): Unit =
  presentImpl()

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

internal expect fun Surface.getCurrentTextureImpl(): Texture?

internal expect fun Surface.presentImpl()

internal expect fun Surface.release()

// endregion
