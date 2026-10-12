package io.github.hhth24340005.gff2027.webgpu

/** The vertex stage of a [RenderPipeline]. */
public class VertexState(
  public val module: ShaderModule,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  public val entryPoint: String,
)
