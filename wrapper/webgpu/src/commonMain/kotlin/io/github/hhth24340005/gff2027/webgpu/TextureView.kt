package io.github.hhth24340005.gff2027.webgpu

/**
 * A view of a [Texture] that a render pass draws to. Obtained from
 * [Texture.createView].
 */
public expect class TextureView

// region internal

internal expect fun TextureView.release()

// endregion
