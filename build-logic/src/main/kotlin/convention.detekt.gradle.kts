import dev.detekt.gradle.Detekt

plugins {
  id("dev.detekt")
}

detekt {
  config.setFrom(rootProject.file("detekt.yml"))
  buildUponDefaultConfig = true
  allRules = true
}

tasks.withType<Detekt>().configureEach {
  val generated =
    layout.buildDirectory
      .dir("generated")
      .get()
      .asFile
  exclude { it.file.startsWith(generated) }
}

tasks.detekt {
  dependsOn("detektCommonMainSourceSet", "detektJvmMainSourceSet")
}
