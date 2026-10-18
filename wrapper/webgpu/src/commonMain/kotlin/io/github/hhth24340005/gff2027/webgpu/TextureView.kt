package io.github.hhth24340005.gff2027.webgpu

/**
 * A view of a [Texture] that a render pass draws to or a shader samples.
 * Obtained from [Texture.createView].
 */
public expect class TextureView : BindingResource

// region internal

internal expect fun TextureView.release()

// endregion
