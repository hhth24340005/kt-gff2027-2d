package io.github.hhth24340005.gff2027.engine2d

import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.webgpu.Device
import io.github.hhth24340005.gff2027.webgpu.Instance
import io.github.hhth24340005.gff2027.webgpu.Surface
import io.github.hhth24340005.gff2027.webgpu.TextureFormat
import io.github.hhth24340005.gff2027.webgpu.configure
import io.github.hhth24340005.gff2027.webgpu.createSurface
import io.github.hhth24340005.gff2027.webgpu.requestAdapter
import io.github.hhth24340005.gff2027.webgpu.requestDevice
import io.github.hhth24340005.gff2027.webgpu.supportedFormats
import io.github.hhth24340005.gff2027.window.Window
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** A window with its own GPU device. */
public class Canvas internal constructor(
  internal val window: Window,
  internal val surface: Surface,
  internal val device: Device,
  internal val format: TextureFormat,
) {
  /** Compiles pipelines for the GPU that draws to this canvas. */
  public val compiler: Compiler = Compiler(this)

  internal val compiledPipelines =
    mutableMapOf<Pipeline<*, *>, Allocated<CompiledPipeline>>()
  internal val compiledPipelinesLock = Mutex()

  // Guarded by compiledPipelinesLock, and keeps compiles from caching after
  // releaseAll
  internal var compiledPipelinesClosed = false
}

/**
 * Creates a canvas that draws to [window]. The scope releases the canvas and
 * every GPU object it made when it exits.
 */
context(scope: ResourceScope)
public suspend fun Canvas(
  window: Window,
): Canvas =
  createCanvas(window)

// region internal

context(scope: ResourceScope)
private suspend fun createCanvas(
  window: Window,
): Canvas {
  val instance = Instance()
  val surface = instance.createSurface(window)
  val adapter =
    instance.requestAdapter(surface) ?: error("No GPU adapter is available")
  val device = adapter.requestDevice()
  val format =
    surface.supportedFormats(adapter).firstOrNull()
      ?: error("The surface supports no format")
  withContext(window.mainDispatcher) {
    surface.configure(device, format, size = window.pixelSize)
  }
  val canvas = Canvas(window, surface, device, format)
  // The scope releases in reverse order, so this runs before the device's
  // release
  scope.onRelease { canvas.releaseAll() }
  return canvas
}

private suspend fun Canvas.releaseAll() {
  compiledPipelinesLock.withLock {
    compiledPipelinesClosed = true
    compiledPipelines.values.forEach { it.release() }
    compiledPipelines.clear()
  }
}

// endregion
