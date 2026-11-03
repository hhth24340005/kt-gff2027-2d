package io.github.hhth24340005.gff2027.engine2d

/**
 * [shader] drawing colors of [format]. It holds no GPU object, so it is
 * cheap to create; a canvas compiles it with its compiler, or when it first
 * draws it, and keeps the result until it closes.
 */
public class Pipeline<Format : ColorFormat, out ShaderType : Shader>(
  public val shader: ShaderType,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  public val format: Format,
)

/** Returns a pipeline of [shader] that draws to a canvas. */
public fun <ShaderType : Shader> Pipeline(
  shader: ShaderType,
): Pipeline<ColorFormat.Canvas, ShaderType> =
  Pipeline(shader, format = Canvas)
