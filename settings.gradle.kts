rootProject.name = "kt-gff2027-2d"

pluginManagement {
  includeBuild("build-logic")

  repositories {
    gradlePluginPortal()
    google()
  }
}

include(":app")
include(":game")
include(":healthcheck")
include(":native:webgpu")
include(":wrapper:webgpu")
include(":wrapper:window")
