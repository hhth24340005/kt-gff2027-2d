package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/**
 * An image on the GPU. Obtained from [Device.createTexture] or
 * [Surface.getCurrentTexture].
 */
public expect class Texture

/**
 * Creates a view of the whole texture. The scope releases it when it exits.
 */
context(scope: ResourceScope)
public suspend fun Texture.createView(): TextureView =
  scope.install(
    acquire = { createViewImpl() },
    release = { view, _ -> view.release() },
  )

// region internal

internal expect fun Texture.createViewImpl(): TextureView

internal expect fun Texture.release()

// endregion
