package io.github.hhth24340005.gff2027.webgpu

/**
 * A target that presents rendered images to a window. Obtained from
 * `Instance.createSurface`, which each platform defines for its windows.
 */
public expect class Surface

// region internal

internal expect fun Surface.release()

// endregion
