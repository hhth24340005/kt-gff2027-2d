package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuRenderBundleEncoderDraw
import wgpu_native.webgpu_h.wgpuRenderBundleEncoderFinish
import wgpu_native.webgpu_h.wgpuRenderBundleEncoderRelease
import wgpu_native.webgpu_h.wgpuRenderBundleEncoderSetBindGroup
import wgpu_native.webgpu_h.wgpuRenderBundleEncoderSetPipeline
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL

public actual class RenderBundleEncoder internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun RenderBundleEncoder.setPipelineImpl(
  pipeline: RenderPipeline,
) {
  wgpuRenderBundleEncoderSetPipeline(handle, pipeline.handle)
}

internal actual fun RenderBundleEncoder.setBindGroupImpl(
  index: Int,
  group: BindGroup,
) {
  wgpuRenderBundleEncoderSetBindGroup(handle, index, group.handle, 0L, NULL)
}

internal actual fun RenderBundleEncoder.drawImpl(
  vertexCount: Int,
  instanceCount: Int,
  firstVertex: Int,
  firstInstance: Int,
) {
  wgpuRenderBundleEncoderDraw(
    handle,
    vertexCount,
    instanceCount,
    firstVertex,
    firstInstance,
  )
}

internal actual fun RenderBundleEncoder.finishImpl(): RenderBundle =
  RenderBundle(wgpuRenderBundleEncoderFinish(handle, NULL))

internal actual fun RenderBundleEncoder.release() {
  wgpuRenderBundleEncoderRelease(handle)
}

// endregion
