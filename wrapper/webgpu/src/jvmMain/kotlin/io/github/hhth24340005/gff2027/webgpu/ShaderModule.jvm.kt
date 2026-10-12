package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuShaderModuleRelease
import java.lang.foreign.MemorySegment

public actual class ShaderModule internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun ShaderModule.release() {
  wgpuShaderModuleRelease(handle)
}

// endregion
