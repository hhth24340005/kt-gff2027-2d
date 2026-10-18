package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/**
 * Shaders and fixed-function state for drawing. Obtained from
 * [Device.createRenderPipeline].
 */
public expect class RenderPipeline

/**
 * Returns the layout of bind group [index], derived from the pipeline's
 * shaders. The scope releases it when it exits.
 */
context(scope: ResourceScope)
public suspend fun RenderPipeline.getBindGroupLayout(
  index: Int,
): BindGroupLayout =
  scope.install(
    acquire = { getBindGroupLayoutImpl(index) },
    release = { layout, _ -> layout.release() },
  )

// region internal

internal expect fun RenderPipeline.getBindGroupLayoutImpl(
  index: Int,
): BindGroupLayout

internal expect fun RenderPipeline.release()

// endregion
