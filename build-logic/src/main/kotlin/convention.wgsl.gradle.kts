// Checks in allTests that the shaders under src/*/wgsl compile on this
// machine's GPU

val wgslValidator: Configuration =
  configurations.create("wgslValidator") {
    isCanBeConsumed = false
  }

dependencies {
  wgslValidator(project(":wrapper:webgpu", "wgslValidator"))
}

val validateWgsl =
  tasks.register<JavaExec>("validateWgsl") {
    description = "Compiles the WGSL shaders under src/*/wgsl."
    group = "verification"
    val shaders = fileTree("src") { include("*/wgsl/**/*.wgsl") }
    val validator: FileCollection = wgslValidator
    val stamp = layout.buildDirectory.file("validateWgsl/stamp")
    inputs.files(shaders)
    inputs.files(validator)
    outputs.file(stamp)
    onlyIf { !shaders.isEmpty }
    classpath =
      files(
        validator.elements.map { installations ->
          val root =
            installations
              .single()
              .asFile
          val jars =
            root
              .resolve("lib")
              .listFiles()
              .orEmpty()
          listOf(root.resolve("classes")) + jars
        },
      )
    mainClass = "io.github.hhth24340005.gff2027.webgpu.WgslValidatorKt"
    argumentProviders.add { shaders.files.map { it.path } }
    jvmArgumentProviders.add {
      listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=${validator.singleFile.resolve("native")}",
      )
    }
    doLast {
      stamp
        .get()
        .asFile
        .writeText("")
    }
  }

tasks.named { it == "allTests" }.configureEach {
  dependsOn(validateWgsl)
}
