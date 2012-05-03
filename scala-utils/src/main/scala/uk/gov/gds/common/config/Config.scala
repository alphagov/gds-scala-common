package uk.gov.gds.common.config

import java.util.Properties
import java.io.{FileInputStream, File, InputStream}
import uk.gov.gds.common.logging.Logging

object Config extends Logging {

  private val testConfigFile = "/test-gds-java-config.properties"
  private val developmentConfigFile = "/development-gds-java-config.properties"
  private val productionConfigFile = "/etc/gds-java-config.properties"

  private lazy val properties = loadConfig(configFileAsStream())

  def apply(name: String, default: String = null) =
    prop(name).getOrElse(Option(default).getOrElse(throw new Exception("Can't find setting: " + name)))

  private def prop(name: String) = Option(properties.get(name).asInstanceOf[String])

  private def configFileAsStream() = if (new File(productionConfigFile).exists()) {
    logger.info("Using production configuration from " + productionConfigFile)
    new FileInputStream(productionConfigFile)
  }
  else {
    logger.info("No production config found in " + productionConfigFile + ", checking for test configuration")

    Option(getClass.getResourceAsStream(testConfigFile)).getOrElse {
      logger.info("No test config found in " + testConfigFile + ", loading dev config from " + developmentConfigFile)
      getClass.getResourceAsStream(developmentConfigFile)
    }
  }

  private def loadConfig(propertyStream: InputStream) = try {
    val props = new Properties
    props.load(propertyStream)
    props
  }
  finally {
    propertyStream.close();
  }
}