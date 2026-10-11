import javax.inject.Inject

plugins {
  id("convention.kotlin-multiplatform")
}

// Adds the HealthCheck launcher to the app image (used by CI).
val healthCheck: Boolean = providers.gradleProperty("healthcheck").isPresent

val nativeLibraries: Configuration =
  configurations.create("nativeLibraries") {
    isCanBeConsumed = false
  }

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(project(":game"))
        if (healthCheck) {
          implementation(project(":healthcheck"))
        }
      }
    }
  }
}

dependencies {
  nativeLibraries(project(":native:webgpu", "nativeLibraries"))
}

abstract class Jpackage : DefaultTask() {
  @get:Inject
  abstract val exec: ExecOperations

  @get:Inject
  abstract val fs: FileSystemOperations

  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val input: DirectoryProperty

  @get:Input
  abstract val appName: Property<String>

  @get:Input
  abstract val mainJar: Property<String>

  @get:Input
  abstract val mainClass: Property<String>

  @get:Input
  abstract val javaOptions: ListProperty<String>

  @get:Input
  @get:Optional
  abstract val healthCheckMainClass: Property<String>

  @get:OutputDirectory
  abstract val destination: DirectoryProperty

  @TaskAction
  fun run() {
    val args =
      mutableListOf<Any>(
        File(System.getProperty("java.home"), "bin/jpackage"),
        "--type",
        "app-image",
        "--name",
        appName.get(),
        "--input",
        input.get().asFile,
        "--main-jar",
        mainJar.get(),
        "--main-class",
        mainClass.get(),
        "--dest",
        destination.get().asFile,
      )
    javaOptions.get().forEach { args.addAll(listOf("--java-options", it)) }
    healthCheckMainClass.orNull?.let {
      val properties = temporaryDir.resolve("HealthCheck.properties")
      properties.writeText("main-class=$it\nwin-console=true\n")
      args.addAll(listOf("--add-launcher", "HealthCheck=$properties"))
    }

    fs.delete { delete(destination) }
    exec.exec { commandLine(args) }
  }
}

val jpackageInput =
  tasks.register<Sync>("jpackageInput") {
    from(configurations.named("jvmRuntimeClasspath"))
    from(nativeLibraries)
    into(layout.buildDirectory.dir("jpackage/input"))
  }

tasks.register<Jpackage>("jpackage") {
  description = "Builds the app image with jpackage."
  input.set(layout.dir(jpackageInput.map { it.destinationDir }))
  appName = rootProject.name
  mainJar = "game-jvm.jar"
  mainClass = "MainKt"
  javaOptions.addAll(
    "--enable-native-access=ALL-UNNAMED",
    "-Djava.library.path=\$APPDIR",
  )
  if (healthCheck) {
    healthCheckMainClass = "healthcheck.MainKt"
  }
  destination = layout.buildDirectory.dir("jpackage/image")
}
