package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope

/** The entry point to WebGPU. Created by calling `Instance()`. */
public expect class Instance

/** Creates an instance. The scope releases it when it exits. */
context(scope: ResourceScope)
public suspend fun Instance(): Instance =
  scope.install(
    acquire = { createInstance() },
    release = { instance, _ -> instance.release() },
  )

/**
 * Requests an adapter. If [compatibleSurface] is given, the adapter can
 * present to it. Returns `null` if no adapter matches.
 */
context(scope: ResourceScope)
public suspend fun Instance.requestAdapter(
  compatibleSurface: Surface? = null,
): Adapter? =
  scope.install(
    acquire = { requestAdapterImpl(compatibleSurface) },
    release = { adapter, _ -> adapter?.release() },
  )

// region internal

internal expect fun createInstance(): Instance

internal expect suspend fun Instance.requestAdapterImpl(
  compatibleSurface: Surface?,
): Adapter?

internal expect fun Instance.release()

// endregion
