package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/**
 * Records GPU commands into a [CommandBuffer]. Obtained from
 * [Device.createCommandEncoder].
 */
public expect class CommandEncoder

/**
 * Records a render pass that draws to [colorAttachments]. The pass ends when
 * [block] returns.
 */
public inline fun CommandEncoder.renderPass(
  colorAttachments: List<RenderPassColorAttachment>,
  block: RenderPassEncoder.() -> Unit,
): Unit =
  renderPassImpl(colorAttachments, block)

/** Finishes recording. The encoder cannot record after this. */
context(scope: ResourceScope)
public suspend fun CommandEncoder.finish(): CommandBuffer =
  scope.install(
    acquire = { finishImpl() },
    release = { commands, _ -> commands.release() },
  )

// region internal

@PublishedApi
internal inline fun CommandEncoder.renderPassImpl(
  colorAttachments: List<RenderPassColorAttachment>,
  block: RenderPassEncoder.() -> Unit,
) {
  val pass = beginRenderPass(colorAttachments)
  try {
    pass.block()
  } finally {
    pass.endAndRelease()
  }
}

@PublishedApi
internal expect fun CommandEncoder.beginRenderPass(
  colorAttachments: List<RenderPassColorAttachment>,
): RenderPassEncoder

internal expect fun CommandEncoder.finishImpl(): CommandBuffer

internal expect fun CommandEncoder.release()

// endregion
