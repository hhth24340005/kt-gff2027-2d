plugins {
  id("convention.kotlin-multiplatform")
  id("convention.kotest")
}

group = "math"
base.archivesName = "math"

kotlin {
  sourceSets.matching { it.name.endsWith("Test") }.configureEach {
    languageSettings.optIn("io.github.hhth24340005.math.OriginApi")
  }
}
