package io.github.hhth24340005.gff2027.webgpu

/** Records draw commands in a [CommandEncoder.renderPass]. */
public expect class RenderPassEncoder

/**
 * Uses [pipeline] for the following draws. See the WebGPU spec:
 * https://www.w3.org/TR/webgpu/#dom-gpurendercommandsmixin-setpipeline
 */
public fun RenderPassEncoder.setPipeline(
  pipeline: RenderPipeline,
): Unit =
  setPipelineImpl(pipeline)

/**
 * Draws primitives with the current pipeline. See the WebGPU spec:
 * https://www.w3.org/TR/webgpu/#dom-gpurendercommandsmixin-draw
 */
public fun RenderPassEncoder.draw(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  vertexCount: Int,
  instanceCount: Int = 1,
  firstVertex: Int = 0,
  firstInstance: Int = 0,
): Unit =
  drawImpl(vertexCount, instanceCount, firstVertex, firstInstance)

/**
 * Uses [group] as bind group [index] for the following draws. See the WebGPU
 * spec:
 * https://www.w3.org/TR/webgpu/#dom-gpubindingcommandsmixin-setbindgroup
 */
public fun RenderPassEncoder.setBindGroup(
  index: Int,
  group: BindGroup,
): Unit =
  setBindGroupImpl(index, group)

// region internal

@PublishedApi
internal expect fun RenderPassEncoder.endAndRelease()

internal expect fun RenderPassEncoder.setPipelineImpl(
  pipeline: RenderPipeline,
)

internal expect fun RenderPassEncoder.drawImpl(
  vertexCount: Int,
  instanceCount: Int,
  firstVertex: Int,
  firstInstance: Int,
)

internal expect fun RenderPassEncoder.setBindGroupImpl(
  index: Int,
  group: BindGroup,
)

// endregion
