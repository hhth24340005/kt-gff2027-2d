package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuRenderPassEncoderDraw
import wgpu_native.webgpu_h.wgpuRenderPassEncoderEnd
import wgpu_native.webgpu_h.wgpuRenderPassEncoderExecuteBundles
import wgpu_native.webgpu_h.wgpuRenderPassEncoderRelease
import wgpu_native.webgpu_h.wgpuRenderPassEncoderSetBindGroup
import wgpu_native.webgpu_h.wgpuRenderPassEncoderSetPipeline
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL
import java.lang.foreign.ValueLayout.ADDRESS

public actual class RenderPassEncoder internal constructor(
  internal val handle: MemorySegment,
)

// region internal

@PublishedApi
internal actual fun RenderPassEncoder.endAndRelease() {
  wgpuRenderPassEncoderEnd(handle)
  wgpuRenderPassEncoderRelease(handle)
}

internal actual fun RenderPassEncoder.setPipelineImpl(
  pipeline: RenderPipeline,
) {
  wgpuRenderPassEncoderSetPipeline(handle, pipeline.handle)
}

internal actual fun RenderPassEncoder.drawImpl(
  vertexCount: Int,
  instanceCount: Int,
  firstVertex: Int,
  firstInstance: Int,
) {
  wgpuRenderPassEncoderDraw(
    handle,
    vertexCount,
    instanceCount,
    firstVertex,
    firstInstance,
  )
}

internal actual fun RenderPassEncoder.setBindGroupImpl(
  index: Int,
  group: BindGroup,
) {
  wgpuRenderPassEncoderSetBindGroup(handle, index, group.handle, 0L, NULL)
}

internal actual fun RenderPassEncoder.executeBundlesImpl(
  bundles: List<RenderBundle>,
) {
  Arena.ofConfined().use { arena ->
    val handles = arena.allocate(ADDRESS, bundles.size.toLong())
    bundles.forEachIndexed { index, bundle ->
      handles.setAtIndex(ADDRESS, index.toLong(), bundle.handle)
    }
    wgpuRenderPassEncoderExecuteBundles(handle, bundles.size.toLong(), handles)
  }
}

// endregion
