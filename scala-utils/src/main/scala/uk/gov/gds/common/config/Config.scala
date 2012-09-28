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

  modeSystemProperty match {
    case Some(mode) => logger.info("System mode is " + mode)
    case _ => logger.info("Production mode")
  }

  def apply(name: String, default: String = null) = prop(name).getOrElse(Option(default).getOrElse(throw new Exception("Can't find setting: " + name)))

  def apply(name: String, default: Int): Int = prop(name) match {
    case Some(value) => value.toInt
    case None => Option(default).getOrElse(throw new Exception("Can't find setting: " + name))
  }

  def apply(name: String, default: Boolean): Boolean = prop(name) match {
    case Some(value) => value.toBoolean
    case None => Option(default).getOrElse(throw new Exception("Can't find setting: " + name))
  }

  private def prop(name: String) = Option(properties.get(name).asInstanceOf[String])

  private def configFileAsStream() = if (new File(productionConfigFile).exists()) {
    logger.info("Using production configuration from " + productionConfigFile)
    new FileInputStream(productionConfigFile)
  }
  else {
    logger.info("No production config found in " + productionConfigFile + ", checking for test or development configuration")

    modeSystemProperty match {
      case Some(mode) if ("test".equals(mode)) => configureForTest
      case _ => configureForDevelopment
    }
  }

  private def modeSystemProperty = {
    Option(System.getProperty("gds.mode"))
  }

  private def configureForTest = openPropertiesFile(testConfigFile)


  private def configureForDevelopment = openPropertiesFile(developmentConfigFile)

  private def openPropertiesFile(filename: String) = Option(getClass.getResource(filename)) match {
    case Some(url) =>
      logger.info("Config file found at: " + url)
      url.openStream()
    case _ =>
      throw new IllegalStateException("Could not open file: " + filename);
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