plugins {
  id("dev.detekt")
}

detekt {
  config.setFrom(rootProject.file("detekt.yml"))
  buildUponDefaultConfig = true
  allRules = true
}

tasks.detekt {
  dependsOn("detektCommonMainSourceSet", "detektJvmMainSourceSet")
}
