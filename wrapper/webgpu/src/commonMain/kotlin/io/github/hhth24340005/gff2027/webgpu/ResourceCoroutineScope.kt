package io.github.hhth24340005.gff2027.webgpu

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/**
 * Runs [block] in a new [CoroutineScope] whose resources are installed into
 * a [ResourceScope].
 *
 * On exit, it waits for every child coroutine of [block] to finish, then
 * releases the installed resources in reverse order.
 */
public suspend fun <R> resourceCoroutineScope(
  block: suspend context(ResourceScope) CoroutineScope.() -> R,
): R =
  resourceScope {
    coroutineScope { block(this@resourceScope, this) }
  }
