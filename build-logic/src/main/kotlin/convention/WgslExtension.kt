package convention

import org.gradle.api.provider.Property

/** Configures convention.wgsl. */
public interface WgslExtension {
  /** The package of the constants generated from the shaders. */
  public val packageName: Property<String>
}
