package io.github.hhth24340005.gff2027.webgpu

/** Memory on the GPU that shaders read. Obtained from [Device.createBuffer]. */
public expect class Buffer : BindingResource

/**
 * The first [size] bytes of [buffer], so that `arrayLength` counts only the
 * elements in them.
 */
public class BufferBinding(
  public val buffer: Buffer,
  public val size: Int,
) : BindingResource

/** A way a [Buffer] may be used. */
public enum class BufferUsage {
  /** [Queue.writeBuffer] can fill it. */
  CopyDst,

  /** Shaders read it as uniforms, as `var<uniform>`. */
  Uniform,

  /** Shaders read it as storage, as `var<storage>`. */
  Storage,

  /** [RenderBundleEncoder.drawIndirect] reads its arguments from it. */
  Indirect,
}

// region internal

internal expect fun Buffer.release()

// endregion
