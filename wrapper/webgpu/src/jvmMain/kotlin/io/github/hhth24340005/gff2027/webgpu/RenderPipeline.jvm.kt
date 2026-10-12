package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuRenderPipelineRelease
import java.lang.foreign.MemorySegment

public actual class RenderPipeline internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun RenderPipeline.release() {
  wgpuRenderPipelineRelease(handle)
}

// endregion
