package io.github.hhth24340005.gff2027.webgpu

/**
 * A queue that runs commands on a [Device]. Obtained from [Device.queue], and
 * released with the device.
 */
public expect class Queue

/** Runs [commands] on the GPU in order. */
public fun Queue.submit(
  commands: List<CommandBuffer>,
): Unit =
  submitImpl(commands)

// region internal

internal expect fun Queue.submitImpl(
  commands: List<CommandBuffer>,
)

// endregion
