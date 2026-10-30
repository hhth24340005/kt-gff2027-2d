plugins {
  id("convention.kotlin-multiplatform")
  id("convention.kotest")
  id("convention.detekt")
}

group = "math"
base.archivesName = "math"

kotlin {
  sourceSets.matching { it.name.endsWith("Test") }.configureEach {
    languageSettings.optIn("io.github.hhth24340005.gff2027.math.OriginApi")
  }
}
