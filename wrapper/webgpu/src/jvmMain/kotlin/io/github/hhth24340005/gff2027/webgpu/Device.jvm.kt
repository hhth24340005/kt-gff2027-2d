package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuDeviceRelease
import java.lang.foreign.MemorySegment

public actual class Device internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun Device.release() {
  wgpuDeviceRelease(handle)
}

// endregion
