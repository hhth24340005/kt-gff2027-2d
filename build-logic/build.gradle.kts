plugins {
  `kotlin-dsl`
}

fun Provider<PluginDependency>.asDependency(): Provider<String> =
  map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

dependencies {
  implementation(
    libs.plugins.kotlin.multiplatform
      .asDependency(),
  )
  implementation(
    libs.plugins.ksp
      .asDependency(),
  )
  implementation(
    libs.plugins.kotest
      .asDependency(),
  )
}
