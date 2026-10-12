package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.webgpu_h.wgpuQueueSubmit
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

// endregion
