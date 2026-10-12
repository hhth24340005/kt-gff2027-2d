package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuCommandBufferRelease
import java.lang.foreign.MemorySegment

public actual class CommandBuffer internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun CommandBuffer.release() {
  wgpuCommandBufferRelease(handle)
}

// endregion
