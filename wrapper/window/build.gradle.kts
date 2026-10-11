plugins {
  id("convention.kotlin-multiplatform")
}

group = "wrapper"
base.archivesName = "wrapper-window"

fun lwjglNativesOf(): String {
  val os = System.getProperty("os.name").lowercase()
  val arch = System.getProperty("os.arch")
  val archSuffix =
    when (arch) {
      "aarch64", "arm64" -> "-arm64"
      "x86_64", "amd64" -> ""
      else -> error("LWJGL does not support architecture: $arch")
    }
  return when {
    "mac" in os -> "natives-macos$archSuffix"
    "windows" in os -> "natives-windows$archSuffix"
    "linux" in os -> "natives-linux$archSuffix"
    else -> error("LWJGL does not support OS: $os")
  }
}

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.lwjgl)
        implementation(libs.lwjgl.sdl)
      }
    }
  }
}

dependencies {
  jvmMainRuntimeOnly(
    variantOf(libs.lwjgl) { classifier(lwjglNativesOf()) },
  )
  jvmMainRuntimeOnly(
    variantOf(libs.lwjgl.sdl) { classifier(lwjglNativesOf()) },
  )
}
