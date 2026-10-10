import java.security.MessageDigest

plugins {
  id("convention.kotlin-multiplatform")
}

repositories {
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
  wgpuNative(
    variantOf(libs.wgpu.native) {
      classifier(wgpuNativePlatformOf())
      artifactType("zip")
    },
  )
}

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
