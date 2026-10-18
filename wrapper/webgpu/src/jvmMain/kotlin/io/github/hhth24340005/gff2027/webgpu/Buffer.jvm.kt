package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuBufferRelease
import java.lang.foreign.MemorySegment

public actual class Buffer internal constructor(
  internal val handle: MemorySegment,
) : BindingResource

// region internal

internal actual fun Buffer.release() {
  wgpuBufferRelease(handle)
}

// endregion
