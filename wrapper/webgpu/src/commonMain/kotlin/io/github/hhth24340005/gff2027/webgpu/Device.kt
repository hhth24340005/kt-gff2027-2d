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
 * Creates a pipeline that draws triangle lists with one sample per pixel. Its
 * bind group layouts are derived from the shaders, and
 * [RenderPipeline.getBindGroupLayout] returns them.
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

/**
 * Creates a buffer of [size] bytes that shaders read as uniforms and
 * [Queue.writeBuffer] can fill. The scope releases it when it exits.
 */
context(scope: ResourceScope)
public suspend fun Device.createBuffer(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  size: Int,
  label: String? = null,
): Buffer =
  scope.install(
    acquire = { createBufferImpl(size, label) },
    release = { buffer, _ -> buffer.release() },
  )

/**
 * Creates a 2D texture of [size], given as width to height, that shaders can
 * sample and [Queue.writeTexture] can fill. The scope releases it when it
 * exits.
 */
context(scope: ResourceScope)
public suspend fun Device.createTexture(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  size: Pair<Int, Int>,
  format: TextureFormat,
  label: String? = null,
): Texture =
  scope.install(
    acquire = { createTextureImpl(size, format, label) },
    release = { texture, _ -> texture.release() },
  )

/**
 * Creates a sampler that clamps coordinates outside a texture to its edge.
 * The scope releases it when it exits.
 *
 * @param magFilter the filter where a texel covers more than a pixel.
 * @param minFilter the filter where a texel covers less than a pixel.
 */
context(scope: ResourceScope)
public suspend fun Device.createSampler(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  magFilter: FilterMode = FilterMode.Nearest,
  minFilter: FilterMode = FilterMode.Nearest,
  label: String? = null,
): Sampler =
  scope.install(
    acquire = { createSamplerImpl(magFilter, minFilter, label) },
    release = { sampler, _ -> sampler.release() },
  )

/**
 * Creates a bind group of [entries] that matches [layout]. The scope releases
 * it when it exits.
 */
context(scope: ResourceScope)
public suspend fun Device.createBindGroup(
  layout: BindGroupLayout,
  entries: List<BindGroupEntry>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  label: String? = null,
): BindGroup =
  scope.install(
    acquire = { createBindGroupImpl(layout, entries, label) },
    release = { group, _ -> group.release() },
  )

/**
 * Creates an encoder that records GPU commands. The scope releases it when
 * it exits.
 */
context(scope: ResourceScope)
public suspend fun Device.createCommandEncoder(): CommandEncoder =
  scope.install(
    acquire = { createCommandEncoderImpl() },
    release = { encoder, _ -> encoder.release() },
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

internal expect fun Device.createBufferImpl(
  size: Int,
  label: String?,
): Buffer

internal expect fun Device.createTextureImpl(
  size: Pair<Int, Int>,
  format: TextureFormat,
  label: String?,
): Texture

internal expect fun Device.createSamplerImpl(
  magFilter: FilterMode,
  minFilter: FilterMode,
  label: String?,
): Sampler

internal expect fun Device.createBindGroupImpl(
  layout: BindGroupLayout,
  entries: List<BindGroupEntry>,
  label: String?,
): BindGroup

internal expect fun Device.createCommandEncoderImpl(): CommandEncoder

internal expect fun Device.release()

// endregion
