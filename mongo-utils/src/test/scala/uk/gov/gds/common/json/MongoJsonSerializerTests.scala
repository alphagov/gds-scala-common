package uk.gov.gds.common.json

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import MongoJsonSerializer._
import org.bson.types.ObjectId
import org.joda.time.{ DateTimeZone, DateTime }

case class TestSerialization(stringField: String = "", arrayField: List[String] = Nil, mapField: Map[String, Int] = Map.empty)

case class MongoSerialisation(id: ObjectId)
case class MongoSerialisationOption(id: Option[ObjectId])

case class DateSerialisation(date: DateTime)

class MongoJsonSerializerTests extends FunSuite with ShouldMatchers {

  test("Should be able to seralise a mongo objectID") {

    val testObjectId = new ObjectId()
    val test = MongoSerialisation(testObjectId)
    val json = toJson(test)

    json should include(testObjectId.toString)
    json should include("""{"id":"""" + testObjectId.toString + """"}""")
  }

  test("Should be able to seralise an option mongo objectID") {

    val testObjectId = new ObjectId()
    val test = MongoSerialisationOption(Some(testObjectId))
    val json = toJson(test)

    json should include(testObjectId.toString)
    json should include("""{"id":"""" + testObjectId.toString + """"}""")
  }

  test("Should be able to deserialise a mongo id") {
    val testObjectId = new ObjectId
    val deserialised = fromJson[MongoSerialisation]("""{"id":"""" + testObjectId.toString + """"}""")
    deserialised.id should be(testObjectId)
  }

  test("Should be able to seralise a date") {
    val testDate = new DateTime(DateTimeZone.UTC)
    val test = DateSerialisation(testDate)
    val json = toJson(test)

    json should include(testDate.toString)
    json should be("""{"date":"""" + testDate.toString() + """"}""")
  }

  test("Should be able to deserialise a date") {
    val testDate = new DateTime(DateTimeZone.UTC)
    val deserialised = fromJson[DateSerialisation]("""{"date":"""" + testDate.toString() + """"}""")
    deserialised.date.toString() should be(testDate.toString())
  }

}