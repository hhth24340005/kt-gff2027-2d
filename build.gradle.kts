plugins {
  alias(libs.plugins.kotlinx.serialization) apply false
  alias(libs.plugins.kotlinx.atomicfu) apply false
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

tasks.register("detekt") {
  group = LifecycleBasePlugin.VERIFICATION_GROUP
  dependsOn(gradle.includedBuild("build-logic").task(":detekt"))
}
