package io.github.hhth24340005.gff2027.webgpu

import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Compiles each WGSL file in [args] on this machine's GPU. Aborts the process
 * at the first shader that fails, as wgpu treats uncaptured errors as fatal.
 */
public fun main(
  args: Array<String>,
): Unit =
  runBlocking { compileAll(args) }

// region internal

private suspend fun compileAll(
  paths: Array<String>,
) {
  resourceCoroutineScope {
    val adapter =
      Instance().requestAdapter() ?: error("No GPU adapter is available")
    val device = adapter.requestDevice()
    for (path in paths) {
      device.createShaderModule(wgsl = File(path).readText(), label = path)
    }
  }
}

// endregion
