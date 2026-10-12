plugins {
  id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
  explicitApi()
  compilerOptions.freeCompilerArgs.addAll(
    "-Xexplicit-context-arguments",
    "-Xreturn-value-checker=full",
    "-Xcollection-literals",
    "-Xname-based-destructuring=complete",
    "-Xcompanion-blocks-and-extensions",
    "-Xconsistent-data-class-copy-visibility",
  )
  jvm()
}

repositories {
  mavenCentral()
}

dependencyLocking {
  lockAllConfigurations()
}
