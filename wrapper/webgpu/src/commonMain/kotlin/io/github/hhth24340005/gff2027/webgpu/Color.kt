package io.github.hhth24340005.gff2027.webgpu

/** A linear RGBA color with components from 0 to 1. */
public class Color(
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  public val red: Double,
  public val green: Double,
  public val blue: Double,
  public val alpha: Double = 1.0,
)
