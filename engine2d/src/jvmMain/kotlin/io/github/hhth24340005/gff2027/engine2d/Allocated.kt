package io.github.hhth24340005.gff2027.engine2d

import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.allocate
import arrow.fx.coroutines.resource
import kotlinx.coroutines.DelicateCoroutinesApi

/** [value], which stays alive until [release] instead of a scope's exit. */
internal class Allocated<out T>(
  val value: T,
  private val releaseImpl: suspend (ExitCase) -> Unit,
) {
  suspend fun release() {
    releaseImpl(ExitCase.Completed)
  }
}

/** Acquires what [acquire] installs, to release with [Allocated.release]. */
internal suspend fun <T> allocate(
  acquire: suspend ResourceScope.() -> T,
): Allocated<T> =
  resource(acquire).toAllocated()

// Canvas releases each one by hand, or all of them when it closes
@OptIn(DelicateCoroutinesApi::class)
internal suspend fun <T> Resource<T>.toAllocated(): Allocated<T> {
  val [value, release] = allocate()
  return Allocated(value, release)
}
