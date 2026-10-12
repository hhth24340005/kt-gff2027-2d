package io.github.hhth24340005.gff2027.window

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED
import org.lwjgl.sdl.SDLEvents.SDL_PushEvent
import org.lwjgl.sdl.SDLVideo.SDL_GetWindowID
import org.lwjgl.sdl.SDL_Event
import kotlin.time.Duration.Companion.seconds

fun main(): Unit =
  runBlocking {
    val closeRequests = CloseRequests()
    Window
      .createOnCurrentDispatcher(
        title = "integration test",
        size = 320 to 240,
      ).use(closeRequests) { window ->
        println(window)
        pushCloseRequest(window)
        withTimeout(1.seconds) { closeRequests.receive() }
      }
    println("OK")
  }

// region internal

private fun pushCloseRequest(
  window: Window,
) {
  SDL_Event.calloc().use { event ->
    event.type(SDL_EVENT_WINDOW_CLOSE_REQUESTED)
    event.window().windowID(SDL_GetWindowID(window.sdlWindow))
    check(SDL_PushEvent(event)) { "SDL_PushEvent failed" }
  }
}

// endregion
