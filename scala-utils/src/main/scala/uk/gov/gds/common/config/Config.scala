package uk.gov.gds.common.config

import java.util.Properties
import java.io.{FileInputStream, File, InputStream}
import uk.gov.gds.common.logging.Logging

object Config extends Logging {

  private val testConfigFile = "/test-gds-java-config.properties"
  private val developmentConfigFile = "/development-gds-java-config.properties"

  // TODO: Currently ertp specific. Genericise
  private val productionConfigFile = "/etc/gds-ertp-config.properties"

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
    Option(getClass.getResource(testConfigFile)) match {
      case Some(url) => {
        logger.info("Test config found at: " + url)
        url.openStream()
      }
      case None => {
        Option(getClass.getResource(developmentConfigFile)) match {
          case Some(url) => {
            logger.info("Development config found at: " + url)
            url.openStream()
          }
        }
      }
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