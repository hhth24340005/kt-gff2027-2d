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
  jvm {
    val main by compilations.getting
    compilations.create("integrationTest") {
      associateWith(main)
    }
  }

  sourceSets {
    jvmMain {
      dependencies {
        api(libs.kotlinx.coroutines.core)
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

val jvmIntegrationTest =
  tasks.register<JavaExec>("jvmIntegrationTest") {
    mustRunAfter(tasks.named("jvmTest"))
    description = "Opens a window and checks that it closes on request."
    group = "verification"
    val compilation =
      kotlin
        .jvm()
        .compilations
        .getByName("integrationTest")
    classpath =
      compilation.output.allOutputs + compilation.runtimeDependencyFiles
    mainClass = "io.github.hhth24340005.gff2027.window.IntegrationTestKt"
    // SDL needs the main thread on macOS
    if ("mac" in System.getProperty("os.name").lowercase()) {
      jvmArgs("-XstartOnFirstThread")
    }
  }

tasks.named("allTests") {
  dependsOn(jvmIntegrationTest)
}
