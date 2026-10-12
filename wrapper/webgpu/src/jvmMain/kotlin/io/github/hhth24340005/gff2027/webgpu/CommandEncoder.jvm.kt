package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.WGPUColor
import wgpu_native.WGPURenderPassColorAttachment
import wgpu_native.WGPURenderPassDescriptor
import wgpu_native.webgpu_h.WGPULoadOp_Clear
import wgpu_native.webgpu_h.WGPULoadOp_Load
import wgpu_native.webgpu_h.WGPUStoreOp_Discard
import wgpu_native.webgpu_h.WGPUStoreOp_Store
import wgpu_native.webgpu_h.WGPU_DEPTH_SLICE_UNDEFINED
import wgpu_native.webgpu_h.wgpuCommandEncoderBeginRenderPass
import wgpu_native.webgpu_h.wgpuCommandEncoderFinish
import wgpu_native.webgpu_h.wgpuCommandEncoderRelease
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL

public actual class CommandEncoder internal constructor(
  internal val handle: MemorySegment,
)

// region internal

@PublishedApi
internal actual fun CommandEncoder.beginRenderPass(
  colorAttachments: List<RenderPassColorAttachment>,
): RenderPassEncoder =
  Arena.ofConfined().use { arena ->
    RenderPassEncoder(
      wgpuCommandEncoderBeginRenderPass(
        handle,
        renderPassDescriptor(arena, colorAttachments),
      ),
    )
  }

internal actual fun CommandEncoder.finishImpl(): CommandBuffer =
  CommandBuffer(wgpuCommandEncoderFinish(handle, NULL))

internal actual fun CommandEncoder.release() {
  wgpuCommandEncoderRelease(handle)
}

private fun renderPassDescriptor(
  arena: Arena,
  colorAttachments: List<RenderPassColorAttachment>,
): MemorySegment {
  val descriptor = WGPURenderPassDescriptor.allocate(arena)
  WGPURenderPassDescriptor
    .colorAttachmentCount(descriptor, colorAttachments.size.toLong())
  WGPURenderPassDescriptor.colorAttachments(
    descriptor,
    renderPassColorAttachments(arena, colorAttachments),
  )
  return descriptor
}

private fun renderPassColorAttachments(
  arena: Arena,
  attachments: List<RenderPassColorAttachment>,
): MemorySegment {
  val structs =
    WGPURenderPassColorAttachment.allocateArray(
      attachments.size.toLong(),
      arena,
    )
  attachments.forEachIndexed { index, attachment ->
    WGPURenderPassColorAttachment
      .asSlice(structs, index.toLong())
      .setColorAttachment(attachment)
  }
  return structs
}

private fun MemorySegment.setColorAttachment(
  attachment: RenderPassColorAttachment,
) {
  WGPURenderPassColorAttachment.view(this, attachment.view.handle)
  WGPURenderPassColorAttachment
    .depthSlice(this, WGPU_DEPTH_SLICE_UNDEFINED())
  when (val load = attachment.load) {
    LoadOp.Load -> {
      WGPURenderPassColorAttachment.loadOp(this, WGPULoadOp_Load())
    }

    is LoadOp.Clear -> {
      WGPURenderPassColorAttachment.loadOp(this, WGPULoadOp_Clear())
      WGPURenderPassColorAttachment.clearValue(this).setColor(load.color)
    }
  }
  WGPURenderPassColorAttachment.storeOp(this, attachment.store.native)
}

private fun MemorySegment.setColor(
  color: Color,
) {
  WGPUColor.r(this, color.red)
  WGPUColor.g(this, color.green)
  WGPUColor.b(this, color.blue)
  WGPUColor.a(this, color.alpha)
}

private val StoreOp.native: Int
  get() =
    when (this) {
      StoreOp.Store -> WGPUStoreOp_Store()
      StoreOp.Discard -> WGPUStoreOp_Discard()
    }

// endregion
