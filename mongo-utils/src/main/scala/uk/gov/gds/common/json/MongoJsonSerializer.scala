package uk.gov.gds.common.json

import java.text.DateFormat
import org.codehaus.jackson.{JsonGenerator, JsonParser, Version}
import org.codehaus.jackson.map.{SerializerProvider, DeserializationContext, JsonDeserializer}
import org.codehaus.jackson.map.module.SimpleModule
import uk.gov.gds.common.logging.Logging
import org.bson.types.ObjectId

object MongoJsonSerializer extends com.codahale.jerkson.Json with Logging {

  val module = new SimpleModule("CustomSerializers", new Version(1, 0, 0, ""))
  addCustomBehaviour(module)
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

  protected def addCustomBehaviour(module: SimpleModule) {
    module.addSerializer(classOf[ObjectId], new ObjectIdSerializer())
    module.addDeserializer[ObjectId](classOf[ObjectId], new ObjectIdDeserializer())
  }

  protected def performCustomConfiguration() {
    mapper.setDateFormat(DateFormat.getDateTimeInstance)
  }
}


private[json] class ObjectIdSerializer extends org.codehaus.jackson.map.JsonSerializer[ObjectId] {

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