package io.github.hhth24340005.gff2027.webgpu

/**
 * The resource types a [BindGroup] holds. Obtained from
 * [RenderPipeline.getBindGroupLayout].
 */
public expect class BindGroupLayout

// region internal

internal expect fun BindGroupLayout.release()

// endregion
