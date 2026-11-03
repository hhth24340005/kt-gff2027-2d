package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.resourceScope
import io.github.hhth24340005.gff2027.window.CloseRequests
import io.github.hhth24340005.gff2027.window.Window
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.lwjgl.sdl.SDLStdinc.SDL_free
import org.lwjgl.sdl.SDLVideo.SDL_GetWindows
import org.lwjgl.sdl.SDLVideo.SDL_SetWindowSize
import org.lwjgl.sdl.SDLVideo.SDL_SyncWindow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Finds out which platforms report the surface outdated after a resize
fun main(): Unit =
  runBlocking {
    withTimeout(60.seconds) { drawsAcrossResize() }
  }

// region internal

private suspend fun drawsAcrossResize() {
  val window =
    Window.createOnCurrentDispatcher(title = "resize", size = SMALL)
  window.use(CloseRequests()) {
    if (window is Window.Offscreen) {
      println("SKIP: the offscreen driver has no surface")
      return@use
    }
    resourceCoroutineScope {
      val instance = Instance()
      val surface = instance.createSurface(window)
      val adapter =
        instance.requestAdapter(surface)
          ?: error("No GPU adapter is available")
      val device = adapter.requestDevice()
      val format =
        surface.supportedFormats(adapter).firstOrNull()
          ?: error("The surface supports no format")
      surface.configure(device, format, size = SMALL)
      drawFrames(device, surface, "before resize")
      resizeOnlyWindow(LARGE)
      // Draws without configuring the surface again
      drawFrames(device, surface, "after resize")
    }
    println("OK: draws across a resize without configure")
  }
}

private suspend fun drawFrames(
  device: Device,
  surface: Surface,
  label: String,
) {
  repeat(FRAMES) { frame ->
    val drawn = drawFrame(device, surface)
    println("$label, frame $frame: ${if (drawn) "drawn" else "skipped"}")
    // Lets the window poll its events
    delay(FRAME_INTERVAL)
  }
}

private suspend fun drawFrame(
  device: Device,
  surface: Surface,
): Boolean =
  resourceScope {
    val texture = surface.getCurrentTexture() ?: return@resourceScope false
    val encoder = device.createCommandEncoder()
    encoder.renderPass(
      [RenderPassColorAttachment(texture.createView(), LoadOp.Clear(GRAY))],
    ) {}
    device.queue.submit([encoder.finish()])
    surface.present()
    true
  }

private fun resizeOnlyWindow(
  size: Pair<Int, Int>,
) {
  val windows = checkNotNull(SDL_GetWindows()) { "SDL_GetWindows failed" }
  try {
    check(windows.remaining() == 1) { "Expected one window" }
    val [width, height] = size
    check(SDL_SetWindowSize(windows[0], width, height)) {
      "SDL_SetWindowSize failed"
    }
    SDL_SyncWindow(windows[0])
  } finally {
    SDL_free(windows)
  }
}

private val SMALL = 320 to 240
private val LARGE = 640 to 480
private const val FRAMES = 5
private val FRAME_INTERVAL = 50.milliseconds
private val GRAY = Color(red = 0.5, green = 0.5, blue = 0.5)

// endregion
