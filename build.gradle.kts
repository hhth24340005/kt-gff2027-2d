plugins {
  alias(libs.plugins.kotlinx.serialization) apply false
  alias(libs.plugins.kotlinx.atomicfu) apply false
  alias(libs.plugins.detekt)
}

repositories {
  mavenCentral()
}

val ktlint: Configuration =
  configurations.create("ktlint") {
    attributes {
      attribute(
        Bundling.BUNDLING_ATTRIBUTE,
        objects.named(Bundling.SHADOWED),
      )
    }
  }

dependencies {
  ktlint(libs.ktlint.cli)
}

val ktlintPatterns: List<String> =
  listOf("**/*.kt", "**/*.kts", "!**/build/**")

tasks.register<JavaExec>("ktlint") {
  group = LifecycleBasePlugin.VERIFICATION_GROUP
  classpath = ktlint
  mainClass = "io.github.ktlint.core.Main"
  args(ktlintPatterns)
}

tasks.register<JavaExec>("ktlintFormat") {
  group = LifecycleBasePlugin.VERIFICATION_GROUP
  classpath = ktlint
  mainClass = "io.github.ktlint.core.Main"
  args("-F")
  args(ktlintPatterns)
}

detekt {
  config.setFrom("detekt.yml")
  source.setFrom(
    subprojects.map { it.file("src") },
    "build-logic/src",
  )
}
