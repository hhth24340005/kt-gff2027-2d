package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/**
 * A physical GPU and its driver. Obtained from [Instance.requestAdapter].
 */
public expect class Adapter

/** Requests a device. The scope releases it when it exits. */
context(scope: ResourceScope)
public suspend fun Adapter.requestDevice(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  label: String? = null,
): Device =
  scope.install(
    acquire = { requestDeviceImpl(label) },
    release = { device, _ -> device.release() },
  )

// region internal

internal expect suspend fun Adapter.requestDeviceImpl(
  label: String?,
): Device

internal expect fun Adapter.release()

// endregion
