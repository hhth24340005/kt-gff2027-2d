package io.github.hhth24340005.gff2027.webgpu

/** Memory on the GPU that shaders read. Obtained from [Device.createBuffer]. */
public expect class Buffer : BindingResource

// region internal

internal expect fun Buffer.release()

// endregion
