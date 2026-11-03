package io.github.hhth24340005.gff2027.webgpu

/**
 * The resource types a [BindGroup] holds. Obtained from
 * [Device.createBindGroupLayout] or [RenderPipeline.getBindGroupLayout].
 */
public expect class BindGroupLayout

/**
 * Declares that `@binding(binding)` holds a resource of [type], which both
 * the vertex and fragment stages can read.
 */
public class BindGroupLayoutEntry(
  public val binding: Int,
  public val type: BindingType,
)

/** The type of resource a [BindGroupLayoutEntry] declares. */
public enum class BindingType {
  /** A 2D texture of floats, as `texture_2d<f32>`. */
  Texture,

  /** A sampler that may blend texels, as `sampler`. */
  Sampler,

  /** A buffer that shaders only read, as `var<storage, read>`. */
  ReadOnlyStorage,
}

// region internal

internal expect fun BindGroupLayout.release()

// endregion
