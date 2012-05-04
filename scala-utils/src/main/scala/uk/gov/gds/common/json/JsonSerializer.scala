package uk.gov.gds.common.json

import com.codahale.jerkson.Json._
import uk.gov.gds.common.logging.Logging

object JsonSerializer extends Logging {

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
      logger.error("Could not serialize FROM json: " + json, e)
      throw e
  }
}
