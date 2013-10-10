package uk.gov.gds.common.json

import _root_.com.fasterxml.jackson.annotation.JsonInclude
import java.text.DateFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import uk.gov.gds.common.logging.Logging
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.{ Type, ParameterizedType }
import com.fasterxml.jackson.databind.DeserializationFeature

trait JsonSerializer extends Logging {

  val mapper = new ObjectMapper()
  mapper.registerModule(new JodaModule())
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
  performCustomConfiguration()

  def toJson(obj: AnyRef) = try {
    mapper.writeValueAsString(obj)
  } catch {
    case e: Exception =>
      logger.error("Could not serialize TO json from: " + obj, e)
      throw e
  }

  def fromJson[A](json: String)(implicit m: Manifest[A]): A = try {
    mapper.readValue(json, typeReference[A])
  } catch {
    case e: Exception =>
      logger.error("Could not deserialize FROM json: " + json, e)
      throw e
  }

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) { m.runtimeClass }
    else {
      new ParameterizedType {
        def getRawType = m.runtimeClass

        def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

        def getOwnerType = null
      }
    }
  }

  protected def performCustomConfiguration() {
    mapper.setDateFormat(DateFormat.getDateTimeInstance)
  }
}

object JsonSerializer extends JsonSerializer