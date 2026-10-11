import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("convention.kotlin-multiplatform")
}

val nativeLibraries: Configuration =
  configurations.create("nativeLibraries") {
    isCanBeConsumed = false
  }

kotlin {
  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  jvm {
    mainRun {
      mainClass = "healthcheck.MainKt"
    }
  }

  sourceSets {
    jvmMain {
      dependencies {
        implementation(project(":native:webgpu"))
      }
    }
  }
}

dependencies {
  nativeLibraries(project(":native:webgpu", "nativeLibraries"))
}

tasks.withType<JavaExec>().configureEach {
  val libraries: FileCollection = nativeLibraries
  inputs.files(libraries)
  jvmArgumentProviders.add {
    listOf(
      "--enable-native-access=ALL-UNNAMED",
      "-Djava.library.path=${libraries.singleFile}",
    )
  }
}
