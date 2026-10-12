package io.github.hhth24340005.gff2027.webgpu

/**
 * The fragment stage of a [RenderPipeline]. Each of [targets] matches the
 * color attachment at the same index in a render pass.
 */
public class FragmentState(
  public val module: ShaderModule,
  public val targets: List<ColorTargetState>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  public val entryPoint: String,
)
