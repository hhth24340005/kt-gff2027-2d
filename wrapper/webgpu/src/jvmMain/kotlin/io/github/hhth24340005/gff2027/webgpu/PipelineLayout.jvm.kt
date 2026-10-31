package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuPipelineLayoutRelease
import java.lang.foreign.MemorySegment

public actual class PipelineLayout internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun PipelineLayout.release() {
  wgpuPipelineLayoutRelease(handle)
}

// endregion
