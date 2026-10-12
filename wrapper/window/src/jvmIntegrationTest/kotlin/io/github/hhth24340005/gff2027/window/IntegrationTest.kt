package io.github.hhth24340005.gff2027.window

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.lwjgl.sdl.SDLError.SDL_GetError
import org.lwjgl.sdl.SDLEvents.SDL_EVENT_QUIT
import org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED
import org.lwjgl.sdl.SDLEvents.SDL_PushEvent
import org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO
import org.lwjgl.sdl.SDLInit.SDL_WasInit
import org.lwjgl.sdl.SDLVideo.SDL_GetWindowID
import org.lwjgl.sdl.SDL_Event
import org.lwjgl.system.Platform
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Scenarios found by an adversarial agent. Each one must leave SDL video
// uninitialized and no window registered for events.
fun main() {
  // Must come first, as SDL takes the first thread to initialize it as the
  // main thread. Only Cocoa needs the actual main thread
  val firstCreateOffMain =
    ("first create off the main thread is rejected" to ::rejectsFirstOffMain)
      .takeIf { Platform.get() == Platform.MACOSX }
  val scenarios =
    listOfNotNull(
      firstCreateOffMain,
      "requests are routed to the right window" to ::routesToTheRightWindow,
      "request for a finished window is ignored" to ::ignoresFinishedWindow,
      "quit reaches every window" to ::quitReachesEveryWindow,
      "request pushed before use is delivered" to ::deliversEarlyRequest,
      "non-positive polling interval is rejected" to ::rejectsBadInterval,
      "out-of-range size is rejected" to ::rejectsBadSize,
      "exception in block is propagated" to ::propagatesException,
      "cancelled caller cleans up" to ::cleansUpOnCancel,
      "already cancelled caller skips block" to ::skipsBlockWhenCancelled,
      "use from a non-main caller" to ::usesFromNonMainCaller,
    )
  for ([name, scenario] in scenarios) {
    runBlocking { withTimeout(20.seconds) { scenario(this) } }
    check(SDL_WasInit(SDL_INIT_VIDEO) == 0) { "$name: video left initialized" }
    check(SdlEvents.closeRequests.isEmpty()) {
      "$name: window left registered"
    }
    println("OK: $name")
  }
}

// region internal

private suspend fun rejectsFirstOffMain(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val result =
    runCatching {
      Window.create(
        title = "off main",
        size = 160 to 120,
        mainDispatcher = Dispatchers.Default,
      )
    }
  check(result.exceptionOrNull() is IllegalArgumentException) {
    "first create off the main thread was not rejected: $result"
  }
  newWindow("on main").use(CloseRequests()) {}
}

private suspend fun routesToTheRightWindow(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requestsA = CloseRequests()
  val requestsB = CloseRequests()
  newWindow("A").use(requestsA) { a ->
    newWindow("B").use(requestsB) { b ->
      pushClose(b)
      check(requestsB.got()) { "B did not get its request" }
      check(requestsA.nothingFor(100.milliseconds)) { "A got B's request" }
      pushClose(a)
      check(requestsA.got()) { "A did not get its request" }
      check(requestsB.nothingFor(100.milliseconds)) { "B got A's request" }
    }
    pushClose(a)
    check(requestsA.got()) { "A stopped receiving after B ended" }
  }
}

private suspend fun ignoresFinishedWindow(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requestsA = CloseRequests()
  val requestsB = CloseRequests()
  newWindow("A").use(requestsA) { a ->
    var idB = 0
    newWindow("B").use(requestsB) { b -> idB = SDL_GetWindowID(b.sdlWindow) }
    pushClose(idB)
    check(requestsB.nothingFor(200.milliseconds)) { "finished B got it" }
    check(requestsA.nothingFor(0.milliseconds)) { "A got B's request" }
    pushClose(a)
    check(requestsA.got()) { "A stopped receiving" }
  }
}

private suspend fun quitReachesEveryWindow(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requestsA = CloseRequests()
  val requestsB = CloseRequests()
  newWindow("A").use(requestsA) {
    newWindow("B").use(requestsB) {
      pushQuit()
      check(requestsB.got()) { "B did not get quit" }
      check(requestsA.got()) { "A did not get quit" }
    }
  }
}

private suspend fun deliversEarlyRequest(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requests = CloseRequests()
  val window = newWindow("early")
  pushClose(window)
  delay(100.milliseconds)
  window.use(requests) {
    check(requests.got()) { "request pushed before use was lost" }
  }
}

private suspend fun rejectsBadInterval(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  newWindow("keeper").use(CloseRequests()) {
    for (interval in listOf(Duration.ZERO, (-1).seconds)) {
      var ran = false
      val result =
        runCatching {
          newWindow("bad").use(CloseRequests(), interval) { ran = true }
        }
      check(result.exceptionOrNull() is IllegalArgumentException) {
        "interval $interval was not rejected: $result"
      }
      check(!ran) { "block ran with interval $interval" }
    }
    check(SDL_WasInit(SDL_INIT_VIDEO) != 0) { "keeper lost its video ref" }
  }
}

private suspend fun rejectsBadSize(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val tooLarge = MAX_WINDOW_SIDE + 1
  for (size in listOf(0 to 1, 1 to 0, tooLarge to 1, 1 to tooLarge)) {
    val result =
      runCatching {
        Window.createOnCurrentDispatcher(title = "bad", size = size)
      }
    result.getOrNull()?.use(CloseRequests()) {}
    check(result.exceptionOrNull() is IllegalArgumentException) {
      "size $size was not rejected: $result"
    }
  }
}

private suspend fun propagatesException(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requests = CloseRequests()
  newWindow("outer").use(requests) { outer ->
    val result =
      runCatching { newWindow("inner").use(CloseRequests()) { error("boom") } }
    check(result.exceptionOrNull()?.message == "boom") {
      "exception was not propagated: $result"
    }
    pushClose(outer)
    check(requests.got()) { "outer stopped receiving after inner threw" }
  }
}

private suspend fun cleansUpOnCancel(
  scope: CoroutineScope,
) {
  val job =
    scope.launch {
      newWindow("cancelled").use(CloseRequests()) { awaitCancellation() }
    }
  delay(100.milliseconds)
  job.cancelAndJoin()
  check(SDL_WasInit(SDL_INIT_VIDEO) == 0) { "not cleaned up after cancel" }
  val result =
    runCatching {
      withTimeout(100.milliseconds) {
        newWindow("timed out").use(CloseRequests()) { awaitCancellation() }
      }
    }
  check(result.isFailure) { "timeout did not propagate" }
}

private suspend fun skipsBlockWhenCancelled(
  scope: CoroutineScope,
) {
  val window = newWindow("skipped")
  var ran = false
  val job =
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      cancel()
      window.use(CloseRequests()) { ran = true }
    }
  job.join()
  check(!ran) { "block ran in a cancelled caller" }
}

private suspend fun usesFromNonMainCaller(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requests = CloseRequests()
  val window = newWindow("non-main caller")
  withContext(Dispatchers.Default) {
    window.use(requests) {
      pushClose(window)
      check(requests.got()) { "request lost when called off main" }
    }
  }
}

private suspend fun newWindow(
  title: String,
): Window =
  Window.createOnCurrentDispatcher(title = title, size = 160 to 120)

private suspend fun CloseRequests.got(): Boolean =
  withTimeoutOrNull(1.seconds) { receive() } != null

private suspend fun CloseRequests.nothingFor(
  duration: Duration,
): Boolean {
  delay(duration)
  return tryReceive().isFailure
}

private fun pushClose(
  window: Window,
) {
  pushClose(SDL_GetWindowID(window.sdlWindow))
}

private fun pushClose(
  windowId: Int,
) {
  SDL_Event.calloc().use { event ->
    event.type(SDL_EVENT_WINDOW_CLOSE_REQUESTED)
    event.window().windowID(windowId)
    check(SDL_PushEvent(event)) { "SDL_PushEvent failed: ${SDL_GetError()}" }
  }
}

private fun pushQuit() {
  SDL_Event.calloc().use { event ->
    event.type(SDL_EVENT_QUIT)
    check(SDL_PushEvent(event)) { "SDL_PushEvent failed: ${SDL_GetError()}" }
  }
}

// endregion
