package io.github.hhth24340005.gff2027.webgpu

/**
 * Shaders and fixed-function state for drawing. Obtained from
 * [Device.createRenderPipeline].
 */
public expect class RenderPipeline

// region internal

internal expect fun RenderPipeline.release()

// endregion
