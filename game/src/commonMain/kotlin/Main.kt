import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

public fun main() {
  KotlinLoggingConfiguration.logStartupMessage = false
  logger.info { "Hello, world!" }
}

private val logger by lazy { KotlinLogging.logger("Main") }
