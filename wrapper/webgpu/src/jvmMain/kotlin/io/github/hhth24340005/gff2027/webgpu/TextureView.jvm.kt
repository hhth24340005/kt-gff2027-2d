package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuTextureViewRelease
import java.lang.foreign.MemorySegment

public actual class TextureView internal constructor(
  internal val handle: MemorySegment,
) : BindingResource

// region internal

internal actual fun TextureView.release() {
  wgpuTextureViewRelease(handle)
}

// endregion
