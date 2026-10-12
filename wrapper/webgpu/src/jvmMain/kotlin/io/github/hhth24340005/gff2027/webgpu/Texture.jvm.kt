package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuTextureCreateView
import wgpu_native.webgpu_h.wgpuTextureRelease
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL

public actual class Texture internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun Texture.createViewImpl(): TextureView =
  TextureView(wgpuTextureCreateView(handle, NULL))

internal actual fun Texture.release() {
  wgpuTextureRelease(handle)
}

// endregion
