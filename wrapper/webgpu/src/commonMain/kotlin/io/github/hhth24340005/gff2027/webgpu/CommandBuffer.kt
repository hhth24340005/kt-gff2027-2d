package io.github.hhth24340005.gff2027.webgpu

/**
 * Recorded commands, ready for [Queue.submit]. Obtained from
 * [CommandEncoder.finish].
 */
public expect class CommandBuffer

// region internal

internal expect fun CommandBuffer.release()

// endregion
