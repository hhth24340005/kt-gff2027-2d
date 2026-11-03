plugins {
  id("convention.kotlin-multiplatform")
  id("convention.detekt")
}

group = "wrapper"
base.archivesName = "wrapper-webgpu"

val nativeLibraries: Configuration =
  configurations.create("nativeLibraries") {
    isCanBeConsumed = false
  }

kotlin {
  jvm {
    val main by compilations.getting
    compilations.create("integrationTest") {
      associateWith(main)
      defaultSourceSet {
        dependencies {
          implementation(libs.lwjgl.sdl)
        }
      }
    }
  }

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

dependencies {
  nativeLibraries(project(":native:webgpu", "nativeLibraries"))
}

val jvmIntegrationTest =
  tasks.register<JavaExec>("jvmIntegrationTest") {
    mustRunAfter(tasks.named("jvmTest"))
    description = "Runs surface scenarios against a real window."
    group = "verification"
    val compilation =
      kotlin
        .jvm()
        .compilations
        .getByName("integrationTest")
    classpath =
      compilation.output.allOutputs + compilation.runtimeDependencyFiles
    mainClass = "io.github.hhth24340005.gff2027.webgpu.IntegrationTestKt"
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

tasks.named("allTests") {
  dependsOn(jvmIntegrationTest)
}
