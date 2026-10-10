import java.security.MessageDigest
import javax.inject.Inject

plugins {
  id("convention.kotlin-multiplatform")
}

repositories {
  exclusiveContent {
    forRepository {
      ivy {
        name = "jextract"
        url = uri("https://download.java.net/java/early_access/jextract/")
        patternLayout {
          artifact("[module]/openjdk-[revision]_[classifier]_bin.[ext]")
        }
        metadataSources { artifact() }
      }
    }
    filter { includeGroup("net.java.jextract") }
  }
  exclusiveContent {
    forRepository {
      ivy {
        name = "wgpu-native"
        url = uri("https://github.com/gfx-rs/wgpu-native/releases/download/")
        patternLayout {
          artifact("[revision]/wgpu-[classifier]-release.[ext]")
        }
        metadataSources { artifact() }
      }
    }
    filter { includeGroup("io.github.gfx-rs") }
  }
}

val jextract: Configuration = configurations.create("jextract")

fun jextractPlatformOf(): String {
  val os = System.getProperty("os.name").lowercase()
  val osPart =
    when {
      "mac" in os -> "macos"
      "windows" in os -> "windows"
      "linux" in os -> "linux"
      else -> error("jextract does not support OS: $os")
    }
  val arch = System.getProperty("os.arch")
  val archPart =
    when (arch) {
      "aarch64", "arm64" -> "aarch64"
      "x86_64", "amd64" -> "x64"
      else -> error("jextract does not support architecture: $arch")
    }
  return "$osPart-$archPart"
}

val wgpuNative: Configuration = configurations.create("wgpuNative")

fun wgpuNativePlatformOf(): String {
  val os = System.getProperty("os.name").lowercase()
  val arch = System.getProperty("os.arch")
  val archPart =
    when (arch) {
      "aarch64", "arm64" -> "aarch64"
      "x86_64", "amd64" -> "x86_64"
      else -> error("wgpu-native does not support architecture: $arch")
    }
  return when {
    "mac" in os -> "macos-$archPart"
    "windows" in os -> "windows-$archPart-msvc"
    "linux" in os -> "linux-$archPart"
    else -> error("wgpu-native does not support OS: $os")
  }
}

val wgpuNativeSha256: Map<String, String> =
  mapOf(
    "macos-aarch64" to
      "a5797a37b1adf720bcd5dcffb291edbbd5b7b14be0a3874c28e6393a655a7a3e",
    "macos-x86_64" to
      "8e2f7378548ddd0e2cf21e7d864dda46e953f0af724855a33778b85ead206d41",
    "linux-aarch64" to
      "015fcdf1dbae82e614a783cc38017e5399ae0927a889fe9b69c9b664bc61b47a",
    "linux-x86_64" to
      "95a4d90c071005a98d03eab348beaa6b07e16eb00d1dcdb9f8348f75eb97ec5a",
    "windows-x86_64-msvc" to
      "7e67d7445c42aeb85e30f88930fd8d7d83ee769e3390aeb1ada75ebf3cf78132",
  )

dependencies {
  jextract(
    variantOf(libs.jextract) {
      classifier(jextractPlatformOf())
      artifactType("tar.gz")
    },
  )
  wgpuNative(
    variantOf(libs.wgpu.native) {
      classifier(wgpuNativePlatformOf())
      artifactType("zip")
    },
  )
}

val installJextract =
  tasks.register<Sync>("installJextract") {
    description =
      "Downloads jextract for the current platform into build/jextract."
    from(tarTree(jextract.elements.map { it.single().asFile }))
    into(layout.buildDirectory.dir("jextract"))
    eachFile {
      // Some files in the archive are read-only, which breaks re-extraction
      val mode = permissions.toUnixNumeric() or 0b010_000_000
      permissions { unix(mode) }
      relativePath =
        RelativePath(
          true,
          *relativePath.segments
            .drop(1)
            .toTypedArray(),
        )
    }
    includeEmptyDirs = false
  }

val extractWgpuNative =
  tasks.register<Sync>("extractWgpuNative") {
    description = "Downloads wgpu-native for the current platform."
    val archive = wgpuNative.elements.map { it.single().asFile }
    val platform = wgpuNativePlatformOf()
    val expected =
      checkNotNull(wgpuNativeSha256[platform]) {
        "No SHA-256 for wgpu-native on $platform"
      }
    from(zipTree(archive))
    into(layout.buildDirectory.dir("wgpu-native"))
    doFirst {
      val actual =
        MessageDigest
          .getInstance("SHA-256")
          .digest(archive.get().readBytes())
          .toHexString()
      check(actual == expected) {
        "SHA-256 mismatch for ${archive.get().name}: $actual"
      }
    }
  }

abstract class Jextract : DefaultTask() {
  @get:Inject
  abstract val exec: ExecOperations

  @get:Inject
  abstract val fs: FileSystemOperations

  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val jextractHome: DirectoryProperty

  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val header: RegularFileProperty

  @get:Input
  abstract val targetPackage: Property<String>

  @get:Input
  abstract val library: Property<String>

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun generate() {
    val windows = "windows" in System.getProperty("os.name").lowercase()
    val executable =
      jextractHome
        .file(if (windows) "bin/jextract.bat" else "bin/jextract")
        .get()
        .asFile
    val headerFile = header.get().asFile

    // Keep only declarations from the header itself, not system headers
    val dump = temporaryDir.resolve("dump-includes.txt")
    exec.exec {
      commandLine(executable, "--dump-includes", dump, headerFile)
    }
    val includes = temporaryDir.resolve("includes.txt")
    includes.writeText(
      dump
        .readLines()
        .filter {
          File(it.substringAfter("# header: ", "")).name == headerFile.name
        }.joinToString("\n"),
    )

    fs.delete { delete(outputDirectory) }
    exec.exec {
      commandLine(
        executable,
        "@$includes",
        "--output",
        outputDirectory.get().asFile,
        "--target-package",
        targetPackage.get(),
        "--library",
        library.get(),
        "--use-system-load-library",
        headerFile,
      )
    }
  }
}

val generateWebgpuBindings =
  tasks.register<Jextract>("generateWebgpuBindings") {
    description = "Generates Java bindings for webgpu.h with jextract."
    jextractHome.set(layout.dir(installJextract.map { it.destinationDir }))
    header.set(
      layout.file(
        extractWgpuNative.map {
          it.destinationDir.resolve("include/webgpu/webgpu.h")
        },
      ),
    )
    targetPackage = "wgpu_native"
    library = "wgpu_native"
    outputDirectory = layout.buildDirectory.dir("generated/jextract")
  }

sourceSets.jvmMain {
  java.srcDir(generateWebgpuBindings)
}

