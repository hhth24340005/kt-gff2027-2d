plugins {
  id("convention.kotlin-multiplatform")
}

group = "wrapper"
base.archivesName = "wrapper-webgpu"

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(project(":native:webgpu"))
      }
    }
  }
}
