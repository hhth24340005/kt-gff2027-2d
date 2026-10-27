package io.github.hhth24340005.gff2027.window

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
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
import kotlin.concurrent.thread
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
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
      "request for an unused window is kept" to ::keepsRequestForUnusedWindow,
      "quit reaches unused windows" to ::quitReachesUnusedWindows,
      "non-positive polling interval is rejected" to ::rejectsBadInterval,
      "out-of-range size is rejected" to ::rejectsBadSize,
      "exception in block is propagated" to ::propagatesException,
      "cancelled caller cleans up" to ::cleansUpOnCancel,
      "cancel right after create cleans up" to ::cleansUpCancelAfterCreate,
      "cancel right after registration cleans up" to
        ::cleansUpCancelAfterRegistration,
      "already cancelled caller skips block" to ::skipsBlockWhenCancelled,
      "use from a non-main caller" to ::usesFromNonMainCaller,
      "second use is rejected" to ::rejectsSecondUse,
      "unconfined dispatcher is rejected" to ::rejectsUnconfined,
    )
  for ([name, scenario] in scenarios) {
    runBlocking { withTimeout(20.seconds) { scenario(this) } }
    checkCleanedUp(name)
  }
  // Runs outside the loop's runBlocking, which would otherwise keep the
  // window's dispatcher running
  val outlived = "window outliving its runBlocking throws"
  withWatchdog(outlived, 20.seconds) { throwsWhenOutlivingRunBlocking() }
  checkCleanedUp(outlived)
}

// region internal

private fun checkCleanedUp(
  name: String,
) {
  check(SDL_WasInit(SDL_INIT_VIDEO) == 0) { "$name: video left initialized" }
  check(SdlEvents.closeRequests.isEmpty()) {
    "$name: window left registered"
  }
  check(SdlEvents.pendingEvents.isEmpty()) {
    "$name: window left with pending events"
  }
  println("OK: $name")
}

// Halts instead of failing, as a hang blocks the main thread for good
private fun withWatchdog(
  name: String,
  timeout: Duration,
  block: () -> Unit,
) {
  val watchdog =
    thread(isDaemon = true) {
      try {
        Thread.sleep(timeout.inWholeMilliseconds)
      } catch (_: InterruptedException) {
        return@thread
      }
      System.err.println("$name: hung for $timeout")
      Runtime.getRuntime().halt(1)
    }
  try {
    block()
  } finally {
    watchdog.interrupt()
  }
}

private fun throwsWhenOutlivingRunBlocking() {
  val window = runBlocking { newWindow("outlived") }
  val result = runCatching { runBlocking { window.use(CloseRequests()) {} } }
  check(result.exceptionOrNull() is IllegalStateException) {
    "using a window after its runBlocking ended did not throw: $result"
  }
  // The window was leaked, and we are back on the main thread
  window.destroy()
}

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

private suspend fun keepsRequestForUnusedWindow(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val a = newWindow("A")
  val b = newWindow("B")
  a.use(CloseRequests()) {
    pushClose(b)
    delay(100.milliseconds)
  }
  val requests = CloseRequests()
  b.use(requests) {
    check(requests.got()) { "request for B was lost while A polled" }
  }
}

private suspend fun quitReachesUnusedWindows(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val a = newWindow("A")
  val b = newWindow("B")
  pushQuit()
  val requestsA = CloseRequests()
  a.use(requestsA) { check(requestsA.got()) { "A did not get quit" } }
  val requestsB = CloseRequests()
  b.use(requestsB) { check(requestsB.got()) { "unused B did not get quit" } }
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

private suspend fun cleansUpCancelAfterCreate(
  scope: CoroutineScope,
) {
  val dispatcher = CancelAfterBlock(currentDispatcher())
  val job =
    scope.launch {
      val _ =
        Window.create(
          title = "cancelled after create",
          size = 160 to 120,
          mainDispatcher = dispatcher,
        )
    }
  dispatcher.target = job
  job.join()
  check(job.isCancelled) { "create was not cancelled" }
}

private suspend fun cleansUpCancelAfterRegistration(
  scope: CoroutineScope,
) {
  val dispatcher = CancelAfterBlock(currentDispatcher())
  val window =
    Window.create(
      title = "cancelled after registration",
      size = 160 to 120,
      mainDispatcher = dispatcher,
    )
  var ran = false
  val job = scope.launch { window.use(CloseRequests()) { ran = true } }
  dispatcher.target = job
  job.join()
  check(job.isCancelled) { "use was not cancelled" }
  check(!ran) { "block ran after registration was cancelled" }
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

private suspend fun rejectsSecondUse(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val requests = CloseRequests()
  val window = newWindow("used twice")
  window.use(requests) {
    val nested = runCatching { window.use(CloseRequests()) {} }
    check(nested.exceptionOrNull() is IllegalStateException) {
      "nested use was not rejected: $nested"
    }
    pushClose(window)
    check(requests.got()) { "window stopped receiving after nested use" }
  }
  val again = runCatching { window.use(CloseRequests()) {} }
  check(again.exceptionOrNull() is IllegalStateException) {
    "second use was not rejected: $again"
  }
}

private suspend fun rejectsUnconfined(
  @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) {
  val window =
    Window.create(
      title = "unconfined",
      size = 160 to 120,
      mainDispatcher = Dispatchers.Unconfined,
    )
  val result =
    runCatching {
      coroutineScope { window.use(CloseRequests()) { delay(1.seconds) } }
    }
  check(result.exceptionOrNull() is IllegalArgumentException) {
    "polling off the main thread was not rejected: $result"
  }
}

// Cancels the caller after each block, before it resumes on its own
// dispatcher, which is when withContext discards the block's result
private class CancelAfterBlock(private val main: CoroutineDispatcher) :
  CoroutineDispatcher() {
  var target: Job? = null

  override fun dispatch(
    context: CoroutineContext,
    block: Runnable,
  ) {
    main.dispatch(context) {
      block.run()
      target?.cancel()
    }
  }
}

private suspend fun currentDispatcher(): CoroutineDispatcher =
  currentCoroutineContext()[ContinuationInterceptor] as CoroutineDispatcher

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
