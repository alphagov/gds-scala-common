package uk.gov.gds.common.json

import java.text.DateFormat
import uk.gov.gds.common.logging.Logging
import org.bson.types.ObjectId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.{ JsonSerializer => JacksonJsonSerializer }
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.{ Type, ParameterizedType }
import com.fasterxml.jackson.databind.DeserializationFeature

object MongoJsonSerializer extends Logging {

  val module = new SimpleModule("CustomSerializers", new Version(1, 0, 0, "", "", ""))
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JodaModule())
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  addCustomBehaviour(module)
  mapper.registerModule(module)
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

  protected def addCustomBehaviour(module: SimpleModule) {
    module.addSerializer(classOf[ObjectId], new ObjectIdSerializer())
    module.addDeserializer[ObjectId](classOf[ObjectId], new ObjectIdDeserializer())
  }

  protected def performCustomConfiguration() {
    mapper.setDateFormat(DateFormat.getDateTimeInstance)
  }
}

private[json] class ObjectIdSerializer extends JacksonJsonSerializer[ObjectId] {

  override def serialize(objectId: ObjectId, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
    jsonGenerator.writeString(objectId.toString)
  }

  override def handledType = classOf[ObjectId]
}

private[json] class ObjectIdDeserializer extends JsonDeserializer[ObjectId] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ObjectId = {
    new ObjectId(jsonParser.getText())
  }
}