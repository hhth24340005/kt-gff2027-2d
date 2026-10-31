package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource

/**
 * Recorded draw commands that render passes can replay with
 * [RenderPassEncoder.executeBundles]. Obtained from
 * [RenderBundleEncoder.finish].
 */
public expect class RenderBundle

/**
 * Returns another reference to this bundle, which keeps it alive until the
 * reference is released, even after the scope that created it exits.
 */
public fun RenderBundle.reference(): Resource<RenderBundle> =
  referenceImpl()

// region internal

internal expect fun RenderBundle.addRef(): RenderBundle

internal expect fun RenderBundle.release()

internal fun RenderBundle.referenceImpl(): Resource<RenderBundle> =
  resource(
    acquire = { addRef() },
    release = { bundle, _ -> bundle.release() },
  )

// endregion
