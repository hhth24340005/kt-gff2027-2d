package io.github.hhth24340005.gff2027.window

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.lwjgl.sdl.SDLError.SDL_GetError
import org.lwjgl.sdl.SDLEvents.SDL_EVENT_QUIT
import org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED
import org.lwjgl.sdl.SDLEvents.SDL_PollEvent
import org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO
import org.lwjgl.sdl.SDLInit.SDL_InitSubSystem
import org.lwjgl.sdl.SDLInit.SDL_IsMainThread
import org.lwjgl.sdl.SDLInit.SDL_QuitSubSystem
import org.lwjgl.sdl.SDLMetal.SDL_Metal_CreateView
import org.lwjgl.sdl.SDLMetal.SDL_Metal_DestroyView
import org.lwjgl.sdl.SDLMetal.SDL_Metal_GetLayer
import org.lwjgl.sdl.SDLProperties.SDL_CreateProperties
import org.lwjgl.sdl.SDLProperties.SDL_DestroyProperties
import org.lwjgl.sdl.SDLProperties.SDL_GetNumberProperty
import org.lwjgl.sdl.SDLProperties.SDL_GetPointerProperty
import org.lwjgl.sdl.SDLProperties.SDL_SetBooleanProperty
import org.lwjgl.sdl.SDLProperties.SDL_SetNumberProperty
import org.lwjgl.sdl.SDLProperties.SDL_SetStringProperty
import org.lwjgl.sdl.SDLVideo.SDL_CreateWindowWithProperties
import org.lwjgl.sdl.SDLVideo.SDL_DestroyWindow
import org.lwjgl.sdl.SDLVideo.SDL_GetCurrentVideoDriver
import org.lwjgl.sdl.SDLVideo.SDL_GetWindowID
import org.lwjgl.sdl.SDLVideo.SDL_GetWindowProperties
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_CREATE_EXTERNAL_GRAPHICS_CONTEXT_BOOLEAN
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_CREATE_FLAGS_NUMBER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_CREATE_TITLE_STRING
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WAYLAND_DISPLAY_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WIN32_HWND_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_WIN32_INSTANCE_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_X11_DISPLAY_POINTER
import org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_X11_WINDOW_NUMBER
import org.lwjgl.sdl.SDLVideo.SDL_WINDOW_METAL
import org.lwjgl.sdl.SDL_Event
import org.lwjgl.system.JNI
import org.lwjgl.system.Platform
import org.lwjgl.system.macosx.LibSystem
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.ContinuationInterceptor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public sealed class Window(
  @PublishedApi internal val sdlWindow: Long,
  @PublishedApi internal val mainDispatcher: CoroutineDispatcher,
) {
  @PublishedApi internal val used: AtomicBoolean = AtomicBoolean(false)

  companion {
    /**
     * Creates a window for the active SDL video driver. Release it with [use].
     *
     * @param size width to height in SDL screen coordinates, each in 1..16384.
     * @param mainDispatcher dispatcher confined to SDL's main thread, which
     * must also be the process main thread on macOS. It is retained for event
     * polling and cleanup.
     * @throws IllegalArgumentException if either size is out of range or
     * creation runs on the wrong thread.
     * @throws IllegalStateException if SDL video initialization, window
     * creation, or Metal view creation fails, or the driver is unsupported.
     */
    public suspend fun create(
      @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
      title: String,
      size: Pair<Int, Int>,
      mainDispatcher: CoroutineDispatcher,
    ): Window =
      createSdlWindow(title, size, mainDispatcher)

    /**
     * Creates a window using the caller's dispatcher for creation, polling,
     * and cleanup. The size limits, thread requirements, and creation errors
     * are the same as for [create]. Release the window with [use].
     *
     * @throws IllegalStateException if the current context has no coroutine
     * dispatcher, or a creation failure described by [create] occurs.
     * @throws IllegalArgumentException if either size is out of range or
     * creation runs on the wrong thread.
     */
    public suspend fun createOnCurrentDispatcher(
      @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
      title: String,
      size: Pair<Int, Int>,
    ): Window =
      createSdlWindow(title, size, currentDispatcher())
  }

  /**
   * Runs [block] in the caller's context and returns its result while polling
   * SDL events on this window's dispatcher. Destroys the window and releases
   * its SDL video reference on exit, including failure or cancellation.
   * Use each window once; its native handles are invalid afterward.
   * Exceptions from [block] propagate after cleanup.
   *
   * @param closeRequests receives this window's close requests and global
   * quit requests, conflated to at most one pending notification. Requests
   * do not end [block] automatically. The channel is left open on exit.
   * @param pollingInterval positive delay between polls when no event is
   * available; defaults to approximately 1/60 second.
   * @throws IllegalArgumentException if the interval is non-positive, the
   * window has no valid SDL ID, or event polling runs off SDL's main thread.
   * Polling failures are reported through the caller's coroutine scope.
   * @throws IllegalStateException if the window is already used.
   */
  public suspend inline fun <R> use(
    closeRequests: CloseRequests,
    pollingInterval: Duration = 1.seconds / 60,
    block: (Window) -> R,
  ): R =
    useImpl(closeRequests, pollingInterval, block)

  public class Cocoa internal constructor(
    sdlWindow: Long,
    mainDispatcher: CoroutineDispatcher,
    @PublishedApi internal val metalView: Long,
    /** Native type: `CAMetalLayer*`. */
    public val metalLayer: Long,
  ) : Window(sdlWindow, mainDispatcher)

  public class Win32 internal constructor(
    sdlWindow: Long,
    mainDispatcher: CoroutineDispatcher,
    /** Native type: `HWND`. */
    public val hwnd: Long,
    /** Native type: `HINSTANCE`. */
    public val hinstance: Long,
  ) : Window(sdlWindow, mainDispatcher)

  public class X11 internal constructor(
    sdlWindow: Long,
    mainDispatcher: CoroutineDispatcher,
    /** Native type: `Display*`. */
    public val display: Long,
    /** Native type: Xlib `Window`, an ID rather than a pointer. */
    public val window: Long,
  ) : Window(sdlWindow, mainDispatcher)

  public class Wayland internal constructor(
    sdlWindow: Long,
    mainDispatcher: CoroutineDispatcher,
    /** Native type: `wl_display*`. */
    public val display: Long,
    /** Native type: `wl_surface*`. */
    public val surface: Long,
  ) : Window(sdlWindow, mainDispatcher)

  /**
   * A window from SDL's `offscreen` driver, chosen with the environment
   * variable `SDL_VIDEO_DRIVER=offscreen`. It has no native surface handles.
   */
  public class Offscreen internal constructor(
    sdlWindow: Long,
    mainDispatcher: CoroutineDispatcher,
  ) : Window(sdlWindow, mainDispatcher)

  // region internal

  @PublishedApi
  internal suspend inline fun <R> useImpl(
    closeRequests: CloseRequests,
    pollingInterval: Duration,
    block: (Window) -> R,
  ): R {
    check(used.compareAndSet(false, true)) { "Window is already used" }
    return try {
      SdlEvents.usePoller(
        this@Window,
        closeRequests,
        pollingInterval,
      ) {
        block(this)
      }
    } finally {
      withContext(mainDispatcher + NonCancellable) { destroy() }
    }
  }

  @PublishedApi
  internal fun destroy() {
    if (this is Cocoa) {
      SDL_Metal_DestroyView(metalView)
    }
    SDL_DestroyWindow(sdlWindow)
    SDL_QuitSubSystem(SDL_INIT_VIDEO)
  }

  // endregion
}

// region internal

// As SDL has a single event queue per process, we use an `object`
@PublishedApi
internal object SdlEvents {
  val closeRequests = mutableMapOf<Int, SendChannel<Unit>>()

  suspend inline fun <R> usePoller(
    window: Window,
    closeRequest: CloseRequests,
    pollingInterval: Duration,
    block: () -> R,
  ): R {
    require(pollingInterval.isPositive()) {
      "Polling interval must be positive, but got: $pollingInterval"
    }
    val windowId = register(window, closeRequest)
    val pollJob =
      CoroutineScope(currentCoroutineContext())
        .launch(window.mainDispatcher) {
          SDL_Event.calloc().use { event ->
            while (true) {
              awaitEvent(pollingInterval, event)
              event.handle()
              yield()
            }
          }
        }
    return try {
      block()
    } finally {
      withContext(window.mainDispatcher + NonCancellable) {
        pollJob.cancelAndJoin()
        closeRequests -= windowId
      }
    }
  }

  @PublishedApi
  internal suspend fun register(
    window: Window,
    closeRequest: CloseRequests,
  ): Int {
    var registered: Int? = null
    try {
      return withContext(window.mainDispatcher) {
        require(SDL_IsMainThread()) {
          "Polling dispatcher must be on the main thread"
        }
        val windowId = SDL_GetWindowID(window.sdlWindow)
        require(windowId != 0) {
          "SDL_GetWindowID failed for window " +
            "0x${window.sdlWindow.toHexString()}: ${SDL_GetError()}"
        }
        closeRequests += windowId to closeRequest.channel
        windowId.also { registered = it }
      }
    } catch (e: CancellationException) {
      // withContext drops the ID if the caller is cancelled before resuming
      registered?.let {
        withContext(window.mainDispatcher + NonCancellable) {
          closeRequests -= it
        }
      }
      throw e
    }
  }

  @PublishedApi
  internal fun SDL_Event.handle() {
    when (this.type()) {
      SDL_EVENT_WINDOW_CLOSE_REQUESTED -> {
        closeRequests[this.window().windowID()]?.trySend(Unit)
      }

      SDL_EVENT_QUIT -> {
        closeRequests.forEach { [_, closeRequest] ->
          closeRequest.trySend(Unit)
        }
      }

      else -> {}
    }
  }

  @PublishedApi
  internal suspend fun awaitEvent(
    interval: Duration,
    event: SDL_Event,
  ) {
    while (true) {
      // The dispatcher may resume us on another thread after `delay`
      require(SDL_IsMainThread()) {
        "Polling dispatcher must be on the main thread"
      }
      if (SDL_PollEvent(event)) {
        return
      }
      delay(interval)
    }
  }
}

// The largest texture side on Metal and D3D12, which a surface cannot exceed
internal const val MAX_WINDOW_SIDE = 16384

private suspend fun currentDispatcher(): CoroutineDispatcher =
  checkNotNull(
    currentCoroutineContext()[ContinuationInterceptor] as? CoroutineDispatcher,
  ) { "No dispatcher in the current context" }

private suspend fun createSdlWindow(
  title: String,
  size: Pair<Int, Int>,
  mainDispatcher: CoroutineDispatcher,
): Window {
  val [width, height] = size
  require(width in 1..MAX_WINDOW_SIDE && height in 1..MAX_WINDOW_SIDE) {
    "Size must be within 1..$MAX_WINDOW_SIDE, but got: $size"
  }
  var created: Window? = null
  try {
    return withContext(mainDispatcher) {
      require(isMainThread()) {
        "Window must be created on the main thread"
      }
      check(SDL_InitSubSystem(SDL_INIT_VIDEO)) {
        "SDL_InitSubSystem failed: ${SDL_GetError()}"
      }
      try {
        openSdlWindow(title, size, mainDispatcher).also { created = it }
      } catch (e: Throwable) {
        SDL_QuitSubSystem(SDL_INIT_VIDEO)
        throw e
      }
    }
  } catch (e: CancellationException) {
    // withContext drops the window if the caller is cancelled before resuming
    created?.let {
      withContext(mainDispatcher + NonCancellable) { it.destroy() }
    }
    throw e
  }
}

// SDL takes the first thread to initialize it as the main thread,
// but Cocoa needs the actual one
private fun isMainThread(): Boolean =
  SDL_IsMainThread() &&
    (Platform.get() != Platform.MACOSX || pthreadMainNp() != 0)

private fun pthreadMainNp(): Int =
  JNI.invokeI(LibSystem.getLibrary().getFunctionAddress("pthread_main_np"))

private fun openSdlWindow(
  title: String,
  size: Pair<Int, Int>,
  mainDispatcher: CoroutineDispatcher,
): Window {
  val driver = SDL_GetCurrentVideoDriver()
  val flags =
    if (driver == "cocoa") {
      SDL_WINDOW_METAL
    } else {
      0L
    }
  val window = createWindowWithExternalContext(title, size, flags)
  check(window != 0L) {
    "SDL_CreateWindowWithProperties failed: ${SDL_GetError()}"
  }
  try {
    return wrapSdlWindow(window, driver, mainDispatcher)
  } catch (e: Throwable) {
    SDL_DestroyWindow(window)
    throw e
  }
}

private fun createWindowWithExternalContext(
  title: String,
  size: Pair<Int, Int>,
  flags: Long,
): Long {
  val props = SDL_CreateProperties()
  try {
    SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, title)
    SDL_SetNumberProperty(
      props,
      SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER,
      size.first.toLong(),
    )
    SDL_SetNumberProperty(
      props,
      SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER,
      size.second.toLong(),
    )
    SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_FLAGS_NUMBER, flags)
    SDL_SetBooleanProperty(
      props,
      SDL_PROP_WINDOW_CREATE_EXTERNAL_GRAPHICS_CONTEXT_BOOLEAN,
      true,
    )
    return SDL_CreateWindowWithProperties(props)
  } finally {
    SDL_DestroyProperties(props)
  }
}

private fun wrapSdlWindow(
  window: Long,
  driver: String?,
  mainDispatcher: CoroutineDispatcher,
): Window {
  val props = SDL_GetWindowProperties(window)
  return when (driver) {
    "cocoa" -> {
      val view = SDL_Metal_CreateView(window)
      check(view != 0L) { "SDL_Metal_CreateView failed: ${SDL_GetError()}" }
      Window.Cocoa(window, mainDispatcher, view, SDL_Metal_GetLayer(view))
    }

    "windows" -> {
      Window.Win32(
        window,
        mainDispatcher,
        props.pointer(SDL_PROP_WINDOW_WIN32_HWND_POINTER),
        props.pointer(SDL_PROP_WINDOW_WIN32_INSTANCE_POINTER),
      )
    }

    "x11" -> {
      Window.X11(
        window,
        mainDispatcher,
        props.pointer(SDL_PROP_WINDOW_X11_DISPLAY_POINTER),
        props.number(SDL_PROP_WINDOW_X11_WINDOW_NUMBER),
      )
    }

    "wayland" -> {
      Window.Wayland(
        window,
        mainDispatcher,
        props.pointer(SDL_PROP_WINDOW_WAYLAND_DISPLAY_POINTER),
        props.pointer(SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER),
      )
    }

    "offscreen" -> {
      Window.Offscreen(window, mainDispatcher)
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
