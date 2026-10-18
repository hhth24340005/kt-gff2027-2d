package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuBindGroupLayoutRelease
import java.lang.foreign.MemorySegment

public actual class BindGroupLayout internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun BindGroupLayout.release() {
  wgpuBindGroupLayoutRelease(handle)
}

// endregion
