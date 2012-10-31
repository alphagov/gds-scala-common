package uk.gov.gds.common.json

import java.text.DateFormat
import org.codehaus.jackson.Version
import org.codehaus.jackson.map.module.SimpleModule
import uk.gov.gds.common.logging.Logging

object JsonSerializer extends com.codahale.jerkson.Json with Logging {

  val module = new SimpleModule("CustomSerializers", new Version(1, 0, 0, ""))
  mapper.registerModule(module)
  performCustomConfiguration()

  def toJson(obj: AnyRef) = try {
    generate(obj)
  }
  catch {
    case e: Exception =>
      logger.error("Could not serialize TO json from: " + obj, e)
      throw e
  }

  def fromJson[A](json: String)(implicit m: Manifest[A]) = try {
    parse[A](json)
  }
  catch {
    case e: Exception =>
      logger.error("Could not deserialize FROM json: " + json, e)
      throw e
  }

  protected def performCustomConfiguration() {
    mapper.setDateFormat(DateFormat.getDateTimeInstance)
  }
}
