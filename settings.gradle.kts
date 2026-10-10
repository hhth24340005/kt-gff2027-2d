rootProject.name = "kt-gff2027-2d"

pluginManagement {
  includeBuild("build-logic")

  repositories {
    gradlePluginPortal()
    google()
  }
}

include(":app")
