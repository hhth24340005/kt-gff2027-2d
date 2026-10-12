package io.github.hhth24340005.gff2027.webgpu

/**
 * A logical connection to an [Adapter] that creates GPU objects.
 * Obtained from [Adapter.requestDevice].
 */
public expect class Device

// region internal

internal expect fun Device.release()

// endregion
