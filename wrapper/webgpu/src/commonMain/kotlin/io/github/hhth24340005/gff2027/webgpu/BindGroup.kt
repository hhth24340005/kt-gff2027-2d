package io.github.hhth24340005.gff2027.webgpu

/**
 * Resources that shaders read together. Obtained from
 * [Device.createBindGroup].
 */
public expect class BindGroup

/** Binds [resource] to `@binding(binding)` in a [BindGroup]. */
public class BindGroupEntry(
  public val binding: Int,
  public val resource: BindingResource,
)

/** A resource that a [BindGroupEntry] binds. */
public expect sealed interface BindingResource

// region internal

internal expect fun BindGroup.release()

// endregion
