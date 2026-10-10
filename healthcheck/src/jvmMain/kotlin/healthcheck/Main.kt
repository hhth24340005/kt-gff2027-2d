package healthcheck

import wgpu_native.WGPUInstanceDescriptor
import wgpu_native.webgpu_h.wgpuCreateInstance
import wgpu_native.webgpu_h.wgpuInstanceRelease
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

public fun main() {
  Arena.ofConfined().use { arena ->
    val descriptor = WGPUInstanceDescriptor.allocate(arena)
    val instance = wgpuCreateInstance(descriptor)
    check(instance != MemorySegment.NULL) { "wgpuCreateInstance failed" }
    wgpuInstanceRelease(instance)
  }
  println("OK")
}
