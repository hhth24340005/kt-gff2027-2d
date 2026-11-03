package io.github.hhth24340005.gff2027.engine2d

import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.webgpu.Instance
import io.github.hhth24340005.gff2027.webgpu.configure
import io.github.hhth24340005.gff2027.webgpu.createSurface
import io.github.hhth24340005.gff2027.webgpu.requestAdapter
import io.github.hhth24340005.gff2027.webgpu.requestDevice
import io.github.hhth24340005.gff2027.webgpu.supportedFormats
import io.github.hhth24340005.gff2027.window.Window
import kotlinx.coroutines.withContext

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
    instance.requestAdapter(surface)
      ?: error("No GPU adapter is available")
  val device = adapter.requestDevice()
  val format =
    surface.supportedFormats(adapter).firstOrNull()
      ?: error("The surface supports no format")
  withContext(window.mainDispatcher) {
    surface.configure(device, format, size = window.pixelSize)
  }
  val canvas = Canvas(window.exposures, surface, device, format)
  // The scope releases in reverse order, so this runs before the device's
  // release
  scope.onRelease { canvas.releaseAll() }
  return canvas
}

// endregion
