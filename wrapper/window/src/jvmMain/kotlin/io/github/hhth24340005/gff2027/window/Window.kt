package io.github.hhth24340005.gff2027.window

import org.lwjgl.sdl.SDLError.SDL_GetError
import org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO
import org.lwjgl.sdl.SDLInit.SDL_Init
import org.lwjgl.sdl.SDLMetal.SDL_Metal_CreateView
import org.lwjgl.sdl.SDLMetal.SDL_Metal_GetLayer
import org.lwjgl.sdl.SDLProperties.SDL_GetNumberProperty
import org.lwjgl.sdl.SDLProperties.SDL_GetPointerProperty
import org.lwjgl.sdl.SDLVideo.SDL_CreateWindow
import org.lwjgl.sdl.SDLVideo.SDL_GetCurrentVideoDriver
import org.lwjgl.sdl.SDLVideo.SDL_GetWindowProperties
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WAYLAND_DISPLAY_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WIN32_HWND_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WIN32_INSTANCE_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_X11_DISPLAY_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_X11_WINDOW_NUMBER
import org.lwjgl.sdl.SDLVideo.SDL_WINDOW_METAL

public sealed interface Window {
  companion {
    public fun create(
      title: String,
      width: Int,
      height: Int,
    ): Window =
      createSdlWindow(title, width, height)
  }

  public data class Cocoa internal constructor(val metalLayer: Long) : Window

  public data class Win32 internal constructor(
    val hwnd: Long,
    val hinstance: Long,
  ) : Window

  public data class X11 internal constructor(
    val display: Long,
    val window: Long,
  ) : Window

  public data class Wayland internal constructor(
    val display: Long,
    val surface: Long,
  ) : Window
}

// region internal

private fun createSdlWindow(
  title: String,
  width: Int,
  height: Int,
): Window {
  check(SDL_Init(SDL_INIT_VIDEO)) { "SDL_Init failed: ${SDL_GetError()}" }
  val driver = SDL_GetCurrentVideoDriver()
  val flags =
    if (driver == "cocoa") {
      SDL_WINDOW_METAL
    } else {
      0L
    }
  val window = SDL_CreateWindow(title, width, height, flags)
  check(window != 0L) { "SDL_CreateWindow failed: ${SDL_GetError()}" }
  val props = SDL_GetWindowProperties(window)
  return when (driver) {
    "cocoa" -> {
      Window.Cocoa(SDL_Metal_GetLayer(SDL_Metal_CreateView(window)))
    }

    "windows" -> {
      Window.Win32(
        props.pointer(SDL_PROP_WINDOW_WIN32_HWND_POINTER),
        props.pointer(SDL_PROP_WINDOW_WIN32_INSTANCE_POINTER),
      )
    }

    "x11" -> {
      Window.X11(
        props.pointer(SDL_PROP_WINDOW_X11_DISPLAY_POINTER),
        props.number(SDL_PROP_WINDOW_X11_WINDOW_NUMBER),
      )
    }

    "wayland" -> {
      Window.Wayland(
        props.pointer(SDL_PROP_WINDOW_WAYLAND_DISPLAY_POINTER),
        props.pointer(SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER),
      )
    }

    else -> {
      error("Unsupported video driver: $driver")
    }
  }
}

private fun Int.pointer(
  name: String,
): Long =
  SDL_GetPointerProperty(this, name, 0L)

private fun Int.number(
  name: String,
): Long =
  SDL_GetNumberProperty(this, name, 0L)

// endregion
