package io.github.hhth24340005.gff2027.webgpu

/**
 * How shaders read a texture, such as the filter between texels. Obtained
 * from [Device.createSampler].
 */
public expect class Sampler

/** How a [Sampler] picks colors between texels. */
public enum class FilterMode {
  /** Takes the closest texel, which keeps pixel art sharp. */
  Nearest,

  /** Blends the surrounding texels. */
  Linear,
}

// region internal

internal expect fun Sampler.release()

// endregion
