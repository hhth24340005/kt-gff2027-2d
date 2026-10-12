package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.window.Window
import wgpu_native.WGPUChainedStruct
import wgpu_native.WGPUSurfaceCapabilities
import wgpu_native.WGPUSurfaceConfiguration
import wgpu_native.WGPUSurfaceDescriptor
import wgpu_native.WGPUSurfaceSourceMetalLayer
import wgpu_native.WGPUSurfaceSourceWaylandSurface
import wgpu_native.WGPUSurfaceSourceWindowsHWND
import wgpu_native.WGPUSurfaceSourceXlibWindow
import wgpu_native.WGPUSurfaceTexture
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceMetalLayer
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceWaylandSurface
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceWindowsHWND
import wgpu_native.webgpu_h.WGPUSType_SurfaceSourceXlibWindow
import wgpu_native.webgpu_h.WGPUStatus_Success
import wgpu_native.webgpu_h.WGPUSurfaceGetCurrentTextureStatus_Lost
import wgpu_native.webgpu_h.WGPUSurfaceGetCurrentTextureStatus_Outdated
import wgpu_native.webgpu_h.WGPUSurfaceGetCurrentTextureStatus_SuccessOptimal
import wgpu_native.webgpu_h.WGPUSurfaceGetCurrentTextureStatus_SuccessSuboptimal
import wgpu_native.webgpu_h.WGPUSurfaceGetCurrentTextureStatus_Timeout
import wgpu_native.webgpu_h.wgpuInstanceCreateSurface
import wgpu_native.webgpu_h.wgpuSurfaceCapabilitiesFreeMembers
import wgpu_native.webgpu_h.wgpuSurfaceConfigure
import wgpu_native.webgpu_h.wgpuSurfaceGetCapabilities
import wgpu_native.webgpu_h.wgpuSurfaceGetCurrentTexture
import wgpu_native.webgpu_h.wgpuSurfacePresent
import wgpu_native.webgpu_h.wgpuSurfaceRelease
import wgpu_native.webgpu_h.wgpuSurfaceUnconfigure
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_INT

/**
 * A target that presents rendered images to a window. Obtained from
 * [Instance.createSurface].
 */
public actual class Surface internal constructor(
  internal val handle: MemorySegment,
) {
  internal var configured: Boolean = false
}

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

internal actual fun Surface.supportedFormatsImpl(
  adapter: Adapter,
): List<TextureFormat> =
  Arena.ofConfined().use { arena ->
    val capabilities = WGPUSurfaceCapabilities.allocate(arena)
    // Fails only for an unknown struct in capabilities.nextInChain, which
    // stays null here
    check(
      wgpuSurfaceGetCapabilities(handle, adapter.handle, capabilities) ==
        WGPUStatus_Success(),
    ) { "wgpuSurfaceGetCapabilities rejected the chain in its output struct" }
    try {
      capabilities.formats().mapNotNull(::textureFormatOf)
    } finally {
      wgpuSurfaceCapabilitiesFreeMembers(capabilities)
    }
  }

internal actual fun Surface.configureImpl(
  device: Device,
  format: TextureFormat,
  width: Int,
  height: Int,
) {
  Arena.ofConfined().use { arena ->
    wgpuSurfaceConfigure(
      handle,
      surfaceConfiguration(arena, device, format, width, height),
    )
  }
  configured = true
}

internal actual fun Surface.getCurrentTextureImpl(): Texture? =
  Arena.ofConfined().use { arena ->
    val surfaceTexture = WGPUSurfaceTexture.allocate(arena)
    wgpuSurfaceGetCurrentTexture(handle, surfaceTexture)
    val texture = WGPUSurfaceTexture.texture(surfaceTexture)
    when (val status = WGPUSurfaceTexture.status(surfaceTexture)) {
      WGPUSurfaceGetCurrentTextureStatus_SuccessOptimal(),
      WGPUSurfaceGetCurrentTextureStatus_SuccessSuboptimal(),
      -> {
        Texture(texture)
      }

      WGPUSurfaceGetCurrentTextureStatus_Timeout(),
      SURFACE_TEXTURE_STATUS_OCCLUDED,
      -> {
        null
      }

      WGPUSurfaceGetCurrentTextureStatus_Outdated(),
      WGPUSurfaceGetCurrentTextureStatus_Lost(),
      -> {
        error("The surface needs configure again (status $status)")
      }

      // Only when the surface is not configured
      else -> {
        error("wgpuSurfaceGetCurrentTexture failed with status $status")
      }
    }
  }

internal actual fun Surface.presentImpl() {
  // Fails only when the surface is not configured
  check(wgpuSurfacePresent(handle) == WGPUStatus_Success()) {
    "wgpuSurfacePresent failed: the surface is not configured"
  }
}

internal actual fun Surface.release() {
  if (configured) {
    wgpuSurfaceUnconfigure(handle)
  }
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

private fun MemorySegment.formats(): List<Int> {
  val count = WGPUSurfaceCapabilities.formatCount(this)
  val formats =
    WGPUSurfaceCapabilities
      .formats(this)
      .reinterpret(count * JAVA_INT.byteSize())
  return List(count.toInt()) { formats.getAtIndex(JAVA_INT, it.toLong()) }
}

private fun surfaceConfiguration(
  arena: Arena,
  device: Device,
  format: TextureFormat,
  width: Int,
  height: Int,
): MemorySegment {
  val configuration = WGPUSurfaceConfiguration.allocate(arena)
  WGPUSurfaceConfiguration.device(configuration, device.handle)
  WGPUSurfaceConfiguration.format(configuration, format.native)
  WGPUSurfaceConfiguration
    .usage(configuration, TEXTURE_USAGE_RENDER_ATTACHMENT)
  WGPUSurfaceConfiguration.width(configuration, width)
  WGPUSurfaceConfiguration.height(configuration, height)
  return configuration
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

// jextract skips it, as webgpu.h declares it as static const
private const val TEXTURE_USAGE_RENDER_ATTACHMENT = 0x10L

// From wgpu.h, which is not bound. Metal reports it while the window is hidden
private const val SURFACE_TEXTURE_STATUS_OCCLUDED = 0x00030001

// endregion
