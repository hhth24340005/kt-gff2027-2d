package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.WGPUExtent3D
import wgpu_native.WGPUTexelCopyBufferLayout
import wgpu_native.WGPUTexelCopyTextureInfo
import wgpu_native.webgpu_h.WGPUTextureAspect_All
import wgpu_native.webgpu_h.WGPU_COPY_STRIDE_UNDEFINED
import wgpu_native.webgpu_h.wgpuQueueSubmit
import wgpu_native.webgpu_h.wgpuQueueWriteTexture
import wgpu_native.webgpu_h.wgpuTextureGetHeight
import wgpu_native.webgpu_h.wgpuTextureGetWidth
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.ADDRESS

public actual class Queue internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun Queue.submitImpl(
  commands: List<CommandBuffer>,
) {
  Arena.ofConfined().use { arena ->
    val handles = arena.allocate(ADDRESS, commands.size.toLong())
    commands.forEachIndexed { index, buffer ->
      handles.setAtIndex(ADDRESS, index.toLong(), buffer.handle)
    }
    wgpuQueueSubmit(handle, commands.size.toLong(), handles)
  }
}

internal actual fun Queue.writeTextureImpl(
  texture: Texture,
  data: IntArray,
  elementsPerRow: Int,
) {
  writeTextureFrom(
    texture,
    MemorySegment.ofArray(data),
    bytesPerRow = elementsPerRow * Int.SIZE_BYTES,
  )
}

private fun Queue.writeTextureFrom(
  texture: Texture,
  data: MemorySegment,
  bytesPerRow: Int,
) {
  Arena.ofConfined().use { arena ->
    wgpuQueueWriteTexture(
      handle,
      texelCopyTextureInfo(arena, texture),
      arena.allocate(data.byteSize()).copyFrom(data),
      data.byteSize(),
      texelCopyBufferLayout(arena, bytesPerRow),
      wholeTextureExtent(arena, texture),
    )
  }
}

private fun texelCopyTextureInfo(
  arena: Arena,
  texture: Texture,
): MemorySegment {
  val info = WGPUTexelCopyTextureInfo.allocate(arena)
  WGPUTexelCopyTextureInfo.texture(info, texture.handle)
  WGPUTexelCopyTextureInfo.aspect(info, WGPUTextureAspect_All())
  return info
}

private fun texelCopyBufferLayout(
  arena: Arena,
  bytesPerRow: Int,
): MemorySegment {
  val layout = WGPUTexelCopyBufferLayout.allocate(arena)
  WGPUTexelCopyBufferLayout.bytesPerRow(layout, bytesPerRow)
  // Zero fails validation, as it is less than the height
  WGPUTexelCopyBufferLayout
    .rowsPerImage(layout, WGPU_COPY_STRIDE_UNDEFINED())
  return layout
}

private fun wholeTextureExtent(
  arena: Arena,
  texture: Texture,
): MemorySegment {
  val extent = WGPUExtent3D.allocate(arena)
  WGPUExtent3D.width(extent, wgpuTextureGetWidth(texture.handle))
  WGPUExtent3D.height(extent, wgpuTextureGetHeight(texture.handle))
  WGPUExtent3D.depthOrArrayLayers(extent, 1)
  return extent
}

// endregion
