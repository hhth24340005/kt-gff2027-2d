package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuRenderBundleAddRef
import wgpu_native.webgpu_h.wgpuRenderBundleRelease
import java.lang.foreign.MemorySegment

public actual class RenderBundle internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun RenderBundle.addRef(): RenderBundle {
  wgpuRenderBundleAddRef(handle)
  return RenderBundle(handle)
}

internal actual fun RenderBundle.release() {
  wgpuRenderBundleRelease(handle)
}

// endregion
