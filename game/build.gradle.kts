import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("convention.kotlin-multiplatform")
  id("convention.kotest")
}

val nativeLibraries: Configuration =
  configurations.create("nativeLibraries") {
    isCanBeConsumed = false
  }

kotlin {
  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  jvm {
    val main = "MainKt"
    binaries {
      executable {
        mainClass = main
      }
    }
    mainRun {
      mainClass = main
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlin.logging)
        implementation(project(":wrapper:webgpu"))
      }
    }

    jvmMain {
      dependencies {
        runtimeOnly(libs.logback.classic)
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
  // SDL needs the main thread on macOS
  if ("mac" in System.getProperty("os.name").lowercase()) {
    jvmArgs("-XstartOnFirstThread")
  }
}
