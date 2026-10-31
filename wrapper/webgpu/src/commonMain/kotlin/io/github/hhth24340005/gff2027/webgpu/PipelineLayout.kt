package io.github.hhth24340005.gff2027.webgpu

/**
 * The bind group layouts a [RenderPipeline] uses, in group order. Obtained
 * from [Device.createPipelineLayout].
 */
public expect class PipelineLayout

// region internal

internal expect fun PipelineLayout.release()

// endregion
