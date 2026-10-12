package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.window.Window
import wgpu_native.WGPUChainedStruct
import wgpu_native.WGPUSurfaceDescriptor
import wgpu_native.WGPUSurfaceSourceMetalLayer
import wgpu_native.WGPUSurfaceSourceWaylandSurface
import wgpu_native.WGPUSurfaceSourceWindowsHWND
import wgpu_native.WGPUSurfaceSourceXlibWindow
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceMetalLayer
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceWaylandSurface
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceWindowsHWND
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceXlibWindow
import wgpu_native.webgpu_h.wgpuInstanceCreateSurface
import wgpu_native.webgpu_h.wgpuSurfaceRelease
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * A target that presents rendered images to a window. Obtained from
 * [Instance.createSurface].
 */
public actual class Surface internal constructor(
  internal val handle: MemorySegment,
)

/**
 * Creates a surface on [window]. Fails for [Window.Offscreen], which has no
 * native surface.
 */
context(scope: ResourceScope)
public suspend fun Instance.createSurface(
  window: Window,
): Surface =
  scope.install(
    acquire = { createSurfaceImpl(window) },
    release = { surface, _ -> surface.release() },
  )

// region internal

internal actual fun Surface.release() {
  wgpuSurfaceRelease(handle)
}

private fun Instance.createSurfaceImpl(
  window: Window,
): Surface =
  Arena.ofConfined().use { arena ->
    Surface(
      wgpuInstanceCreateSurface(handle, surfaceDescriptor(arena, window)),
    )
  }

private fun surfaceDescriptor(
  arena: Arena,
  window: Window,
): MemorySegment {
  val descriptor = WGPUSurfaceDescriptor.allocate(arena)
  WGPUSurfaceDescriptor.nextInChain(descriptor, window.source(arena))
  return descriptor
}

private fun Window.source(
  arena: Arena,
): MemorySegment =
  when (this) {
    is Window.Cocoa -> {
      WGPUSurfaceSourceMetalLayer.allocate(arena).also {
        WGPUChainedStruct.sType(
          WGPUSurfaceSourceMetalLayer.chain(it),
          WGPUSType_SurfaceSourceMetalLayer(),
        )
        WGPUSurfaceSourceMetalLayer
          .layer(it, MemorySegment.ofAddress(metalLayer))
      }
    }

    is Window.Win32 -> {
      WGPUSurfaceSourceWindowsHWND.allocate(arena).also {
        WGPUChainedStruct.sType(
          WGPUSurfaceSourceWindowsHWND.chain(it),
          WGPUSType_SurfaceSourceWindowsHWND(),
        )
        WGPUSurfaceSourceWindowsHWND
          .hinstance(it, MemorySegment.ofAddress(hinstance))
        WGPUSurfaceSourceWindowsHWND.hwnd(it, MemorySegment.ofAddress(hwnd))
      }
    }

    is Window.X11 -> {
      WGPUSurfaceSourceXlibWindow.allocate(arena).also {
        WGPUChainedStruct.sType(
          WGPUSurfaceSourceXlibWindow.chain(it),
          WGPUSType_SurfaceSourceXlibWindow(),
        )
        WGPUSurfaceSourceXlibWindow
          .display(it, MemorySegment.ofAddress(display))
        WGPUSurfaceSourceXlibWindow.window(it, window)
      }
    }

    is Window.Wayland -> {
      WGPUSurfaceSourceWaylandSurface.allocate(arena).also {
        WGPUChainedStruct.sType(
          WGPUSurfaceSourceWaylandSurface.chain(it),
          WGPUSType_SurfaceSourceWaylandSurface(),
        )
        WGPUSurfaceSourceWaylandSurface
          .display(it, MemorySegment.ofAddress(display))
        WGPUSurfaceSourceWaylandSurface
          .surface(it, MemorySegment.ofAddress(surface))
      }
    }

    is Window.Offscreen -> {
      error("The offscreen driver has no surface to draw on")
    }
  }

// endregion
