plugins {
  id("convention.kotlin-multiplatform")
}

group = "wrapper"
base.archivesName = "wrapper-webgpu"

kotlin {
  compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

  sourceSets {
    commonMain {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.arrow.fx.coroutines)
      }
    }

    jvmMain {
      dependencies {
        implementation(project(":native:webgpu"))
        api(project(":wrapper:window"))
      }
    }
  }
}
