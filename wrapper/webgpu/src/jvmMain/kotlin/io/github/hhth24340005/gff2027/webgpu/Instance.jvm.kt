package io.github.hhth24340005.gff2027.webgpu

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import wgpu_native.WGPURequestAdapterCallback
import wgpu_native.WGPURequestAdapterCallbackInfo
import wgpu_native.WGPURequestAdapterOptions
import wgpu_native.webgpu_h.WGPUCallbackMode_AllowSpontaneous
import wgpu_native.webgpu_h.WGPURequestAdapterStatus_CallbackCancelled
import wgpu_native.webgpu_h.WGPURequestAdapterStatus_Success
import wgpu_native.webgpu_h.WGPURequestAdapterStatus_Unavailable
import wgpu_native.webgpu_h.wgpuCreateInstance
import wgpu_native.webgpu_h.wgpuInstanceProcessEvents
import wgpu_native.webgpu_h.wgpuInstanceRelease
import wgpu_native.webgpu_h.wgpuInstanceRequestAdapter
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL

public actual class Instance internal constructor(
  internal val handle: MemorySegment,
)

// region internal

internal actual fun createInstance(): Instance =
  Instance(wgpuCreateInstance(NULL))

internal actual suspend fun Instance.requestAdapterImpl(
  compatibleSurface: Surface?,
): Adapter? =
  Arena.ofShared().use { arena ->
    awaitCallback(release = { it?.release() }) { adapter ->
      wgpuInstanceRequestAdapter(
        arena,
        handle,
        requestAdapterOptions(arena, compatibleSurface),
        requestAdapterCallbackInfo(arena, adapter),
      )
    }
  }

internal actual fun Instance.release() {
  wgpuInstanceRelease(handle)
}

// Callbacks arrive only while someone processes events, so pump them while
// waiting. A cancelled caller still waits for the callback, because it lives
// in the caller's arena, and releases what the callback delivers
internal suspend fun <T> Instance.awaitCallback(
  release: (T) -> Unit,
  request: (CompletableDeferred<T>) -> Unit,
): T {
  val result = CompletableDeferred<T>()
  request(result)
  try {
    pumpUntil(result)
  } catch (e: CancellationException) {
    withContext(NonCancellable) { pumpUntil(result) }
    if (result.getCompletionExceptionOrNull() == null) {
      release(result.getCompleted())
    }
    throw e
  }
  return result.getCompleted()
}

private suspend fun Instance.pumpUntil(
  callback: Deferred<*>,
) {
  while (!callback.isCompleted) {
    wgpuInstanceProcessEvents(handle)
    yield()
  }
}

private fun requestAdapterOptions(
  arena: Arena,
  compatibleSurface: Surface?,
): MemorySegment {
  val options = WGPURequestAdapterOptions.allocate(arena)
  WGPURequestAdapterOptions
    .compatibleSurface(options, compatibleSurface?.handle ?: NULL)
  return options
}

private fun Instance.requestAdapterCallbackInfo(
  arena: Arena,
  adapter: CompletableDeferred<Adapter?>,
): MemorySegment {
  val callback =
    WGPURequestAdapterCallback.allocate(
      { status, result, message, _, _ ->
        when (status) {
          WGPURequestAdapterStatus_Success() -> {
            adapter.complete(Adapter(result, this))
          }

          WGPURequestAdapterStatus_Unavailable() -> {
            adapter.complete(null)
          }

          // Only when the instance is released before the callback, which
          // the resource scope prevents by releasing it after the adapter
          WGPURequestAdapterStatus_CallbackCancelled() -> {
            adapter.completeExceptionally(
              IllegalStateException(
                "Adapter request was cancelled: the instance was released",
              ),
            )
          }

          else -> {
            adapter.completeExceptionally(
              IllegalStateException(
                "Adapter request failed: ${message.stringView()}",
              ),
            )
          }
        }
      },
      arena,
    )
  val info = WGPURequestAdapterCallbackInfo.allocate(arena)
  WGPURequestAdapterCallbackInfo
    .mode(info, WGPUCallbackMode_AllowSpontaneous())
  WGPURequestAdapterCallbackInfo.callback(info, callback)
  return info
}

// endregion
