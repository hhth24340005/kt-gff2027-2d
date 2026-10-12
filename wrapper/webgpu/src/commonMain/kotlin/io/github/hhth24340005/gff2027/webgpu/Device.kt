package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/**
 * A logical connection to an [Adapter] that creates GPU objects.
 * Obtained from [Adapter.requestDevice].
 */
public expect class Device {
  /** The queue that runs commands submitted to this device. */
  public val queue: Queue
}

/**
 * Compiles [wgsl], a shader written in [WGSL](https://www.w3.org/TR/WGSL/).
 * The scope releases the module when it exits.
 */
context(scope: ResourceScope)
public suspend fun Device.createShaderModule(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  wgsl: String,
  label: String? = null,
): ShaderModule =
  scope.install(
    acquire = { createShaderModuleImpl(wgsl, label) },
    release = { module, _ -> module.release() },
  )

/**
 * Creates a pipeline that draws triangle lists with one sample per pixel.
 */
context(scope: ResourceScope)
public suspend fun Device.createRenderPipeline(
  vertex: VertexState,
  fragment: FragmentState,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  label: String? = null,
): RenderPipeline =
  scope.install(
    acquire = { createRenderPipelineImpl(vertex, fragment, label) },
    release = { pipeline, _ -> pipeline.release() },
  )

// region internal

internal expect fun Device.createShaderModuleImpl(
  wgsl: String,
  label: String?,
): ShaderModule

internal expect fun Device.createRenderPipelineImpl(
  vertex: VertexState,
  fragment: FragmentState,
  label: String?,
): RenderPipeline

internal expect fun Device.release()

// endregion
