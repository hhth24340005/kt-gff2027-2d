package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.WGPUStringView
import wgpu_native.webgpu_h.WGPU_STRLEN
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL
import java.lang.foreign.ValueLayout.JAVA_BYTE

// region internal

internal fun MemorySegment.setStringView(
  arena: Arena,
  value: String,
) {
  WGPUStringView.data(this, arena.allocateFrom(value))
  WGPUStringView.length(this, WGPU_STRLEN())
}

internal fun MemorySegment.stringView(): String {
  val data = WGPUStringView.data(this)
  val length = WGPUStringView.length(this)
  return when {
    data == NULL -> ""
    length == WGPU_STRLEN() -> data.reinterpret(Long.MAX_VALUE).getString(0)
    else -> String(data.reinterpret(length).toArray(JAVA_BYTE))
  }
}

// endregion
