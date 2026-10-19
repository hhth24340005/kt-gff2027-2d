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
    compilations.create("wgslValidator") {
      associateWith(main)
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

// Lays out the validator for convention.wgsl: its classes, its jars, and the
// native libraries it loads
val installWgslValidator =
  tasks.register<Sync>("installWgslValidator") {
    description = "Installs the WGSL validator into build/wgsl-validator."
    val compilation =
      kotlin
        .jvm()
        .compilations
        .getByName("wgslValidator")
    val dependencies = compilation.runtimeDependencyFiles
    from(compilation.output.allOutputs) { into("classes") }
    from(dependencies.filter { it.isDirectory }) { into("classes") }
    from(dependencies.filter { it.isFile }) { into("lib") }
    from(nativeLibraries) { into("native") }
    into(layout.buildDirectory.dir("wgsl-validator"))
  }

configurations.consumable("wgslValidator")

artifacts.add(
  "wgslValidator",
  installWgslValidator.map { it.destinationDir },
) {
  builtBy(installWgslValidator)
}
