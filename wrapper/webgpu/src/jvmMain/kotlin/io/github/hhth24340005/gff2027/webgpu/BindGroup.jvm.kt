package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuBindGroupRelease
import java.lang.foreign.MemorySegment

public actual class BindGroup internal constructor(
  internal val handle: MemorySegment,
)

public actual sealed interface BindingResource

// region internal

internal actual fun BindGroup.release() {
  wgpuBindGroupRelease(handle)
}

// endregion
