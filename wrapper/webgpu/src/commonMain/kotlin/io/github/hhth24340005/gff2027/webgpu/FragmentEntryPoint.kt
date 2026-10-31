package io.github.hhth24340005.gff2027.webgpu

/** The function in a [ShaderModule] that runs the fragment stage. */
public sealed interface FragmentEntryPoint {
  /** The only `@fragment` function in the module. */
  public data object Auto : FragmentEntryPoint

  /** The `@fragment` function named [name]. */
  public class Named(public val name: String) : FragmentEntryPoint
}

/** Returns the `@fragment` function named [name]. */
public fun FragmentEntryPoint(
  name: String,
): FragmentEntryPoint =
  FragmentEntryPoint.Named(name)
