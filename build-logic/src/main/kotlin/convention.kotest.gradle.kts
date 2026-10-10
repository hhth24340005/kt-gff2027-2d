import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.google.devtools.ksp")
  id("io.kotest")
}

val libs: VersionCatalog = the<VersionCatalogsExtension>().named("libs")

kotlin {
  sourceSets {
    commonTest {
      dependencies {
        implementation(libs.findBundle("kotest").get())
      }
    }

    jvmTest {
      dependencies {
        implementation(libs.findLibrary("kotest-runner-junit5").get())
      }
    }
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

tasks.withType<AbstractTestTask>().configureEach {
  testLogging {
    events("passed", "skipped", "failed")
    exceptionFormat = TestExceptionFormat.FULL
    showStandardStreams = true
    showStackTraces = false
  }
}
