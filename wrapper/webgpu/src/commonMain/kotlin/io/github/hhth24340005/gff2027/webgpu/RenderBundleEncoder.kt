package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/**
 * Records draw commands into a [RenderBundle]. Obtained from
 * [Device.createRenderBundleEncoder].
 */
public expect class RenderBundleEncoder

/**
 * Uses [pipeline] for the following draws. See the WebGPU spec:
 * https://www.w3.org/TR/webgpu/#dom-gpurendercommandsmixin-setpipeline
 */
public fun RenderBundleEncoder.setPipeline(
  pipeline: RenderPipeline,
): Unit =
  setPipelineImpl(pipeline)

/**
 * Uses [group] as bind group [index] for the following draws. See the WebGPU
 * spec:
 * https://www.w3.org/TR/webgpu/#dom-gpubindingcommandsmixin-setbindgroup
 */
public fun RenderBundleEncoder.setBindGroup(
  index: Int,
  group: BindGroup,
): Unit =
  setBindGroupImpl(index, group)

/**
 * Draws primitives with the current pipeline. See the WebGPU spec:
 * https://www.w3.org/TR/webgpu/#dom-gpurendercommandsmixin-draw
 */
public fun RenderBundleEncoder.draw(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  vertexCount: Int,
  instanceCount: Int = 1,
  firstVertex: Int = 0,
  firstInstance: Int = 0,
): Unit =
  drawImpl(vertexCount, instanceCount, firstVertex, firstInstance)

/**
 * Draws primitives with the current pipeline, reading `vertexCount`,
 * `instanceCount`, `firstVertex` and `firstInstance` as four u32 from
 * [buffer] at [offset] bytes when the GPU runs the draw. See the WebGPU spec:
 * https://www.w3.org/TR/webgpu/#dom-gpurendercommandsmixin-drawindirect
 */
public fun RenderBundleEncoder.drawIndirect(
  buffer: Buffer,
  offset: Int = 0,
): Unit =
  drawIndirectImpl(buffer, offset)

/**
 * Finishes recording. The encoder cannot record after this. The bundle keeps
 * the pipelines and bind groups it uses alive, so the scopes that created
 * them may exit before it.
 */
context(scope: ResourceScope)
public suspend fun RenderBundleEncoder.finish(): RenderBundle =
  scope.install(
    acquire = { finishImpl() },
    release = { bundle, _ -> bundle.release() },
  )

// region internal

internal expect fun RenderBundleEncoder.setPipelineImpl(
  pipeline: RenderPipeline,
)

internal expect fun RenderBundleEncoder.setBindGroupImpl(
  index: Int,
  group: BindGroup,
)

internal expect fun RenderBundleEncoder.drawImpl(
  vertexCount: Int,
  instanceCount: Int,
  firstVertex: Int,
  firstInstance: Int,
)

internal expect fun RenderBundleEncoder.drawIndirectImpl(
  buffer: Buffer,
  offset: Int,
)

internal expect fun RenderBundleEncoder.finishImpl(): RenderBundle

internal expect fun RenderBundleEncoder.release()

// endregion
