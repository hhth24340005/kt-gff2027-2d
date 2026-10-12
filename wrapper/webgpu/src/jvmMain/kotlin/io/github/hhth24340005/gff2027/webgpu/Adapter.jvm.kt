package io.github.hhth24340005.gff2027.webgpu

import kotlinx.coroutines.CompletableDeferred
import wgpu_native.WGPUDeviceDescriptor
import wgpu_native.WGPURequestDeviceCallback
import wgpu_native.WGPURequestDeviceCallbackInfo
import wgpu_native.webgpu_h.WGPUCallbackMode_AllowSpontaneous
import wgpu_native.webgpu_h.WGPURequestDeviceStatus_CallbackCancelled
import wgpu_native.webgpu_h.WGPURequestDeviceStatus_Success
import wgpu_native.webgpu_h.wgpuAdapterRelease
import wgpu_native.webgpu_h.wgpuAdapterRequestDevice
import wgpu_native.webgpu_h.wgpuDeviceRelease
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

public actual class Adapter internal constructor(
  internal val handle: MemorySegment,
  internal val instance: Instance,
)

// region internal

internal actual suspend fun Adapter.requestDeviceImpl(
  label: String?,
): Device {
  val deviceHandle =
    Arena.ofShared().use { arena ->
      instance.awaitCallback(release = ::wgpuDeviceRelease) { device ->
        wgpuAdapterRequestDevice(
          arena,
          handle,
          deviceDescriptor(arena, label),
          requestDeviceCallbackInfo(arena, device),
        )
      }
    }
  return Device(deviceHandle)
}

internal actual fun Adapter.release() {
  wgpuAdapterRelease(handle)
}

private fun deviceDescriptor(
  arena: Arena,
  label: String?,
): MemorySegment {
  val descriptor = WGPUDeviceDescriptor.allocate(arena)
  if (label != null) {
    WGPUDeviceDescriptor.label(descriptor).setStringView(arena, label)
  }
  return descriptor
}

private fun requestDeviceCallbackInfo(
  arena: Arena,
  device: CompletableDeferred<MemorySegment>,
): MemorySegment {
  val callback =
    WGPURequestDeviceCallback.allocate(
      { status, result, message, _, _ ->
        when (status) {
          // Only the handle, because constructing a Device may call into
          // webgpu.h, which is unsafe in a spontaneous callback
          WGPURequestDeviceStatus_Success() -> {
            device.complete(result)
          }

          // Only when the instance is released before the callback, which
          // the resource scope prevents by releasing it after the device
          WGPURequestDeviceStatus_CallbackCancelled() -> {
            device.completeExceptionally(
              IllegalStateException(
                "Device request was cancelled: the instance was released",
              ),
            )
          }

          else -> {
            device.completeExceptionally(
              IllegalStateException(
                "Device request failed: ${message.stringView()}",
              ),
            )
          }
        }
      },
      arena,
    )
  val info = WGPURequestDeviceCallbackInfo.allocate(arena)
  WGPURequestDeviceCallbackInfo.mode(info, WGPUCallbackMode_AllowSpontaneous())
  WGPURequestDeviceCallbackInfo.callback(info, callback)
  return info
}

// endregion
