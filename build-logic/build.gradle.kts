buildscript {
  dependencyLocking {
    lockAllConfigurations()
  }
}

plugins {
  `kotlin-dsl`
  alias(libs.plugins.detekt)
}

dependencyLocking {
  lockAllConfigurations()
}

detekt {
  config.setFrom(file("../detekt.yml"))
  buildUponDefaultConfig = true
  allRules = true
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
  implementation(
    libs.plugins.detekt
      .asDependency(),
  )
}
