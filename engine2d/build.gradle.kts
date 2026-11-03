plugins {
  id("convention.kotlin-multiplatform")
  id("convention.kotest")
  id("convention.detekt")
}

group = "engine2d"
base.archivesName = "engine2d"

kotlin {
  sourceSets.configureEach {
    languageSettings.optIn("io.github.hhth24340005.gff2027.math.OriginApi")
    languageSettings.optIn("io.github.hhth24340005.gff2027.math.RawValueApi")
  }

  sourceSets {
    commonMain {
      dependencies {
        api(project(":math"))
        api(project(":wrapper:window"))
        api(libs.arrow.fx.coroutines)
        implementation(project(":wrapper:webgpu"))
      }
    }
  }
}
