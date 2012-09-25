package uk.gov.gds.common.json

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import JsonSerializer._
import org.bson.types.ObjectId
import org.joda.time.{DateTimeZone, DateTime}

case class TestSerialization(stringField: String = "", arrayField: List[String] = Nil, mapField: Map[String, Int] = Map.empty)

case class MongoSerialisation(id: ObjectId)
case class MongoSerialisationOption(id: Option[ObjectId])

case class DateSerialisation(date: DateTime)

class JsonSerializerTests extends FunSuite with ShouldMatchers {

  test("Should be able to turn map to json string") {
    val json = toJson(Map("this" -> "string"))
    json should include( """{"this":"string"}""")
  }

  test("should be able to turn case class into json string") {
    val test = TestSerialization(stringField = "this is a string", arrayField = List("1", "2", "3"), mapField = Map("key" -> 100))
    val json = toJson(test)
    json should include( """"stringField":"this is a string"""")
    json should include( """"arrayField":["1","2","3"]""")
    json should include( """"mapField":{"key":100}""")
  }

  test("Should be able to reconstruct the case class fron json string") {
    val test = TestSerialization(stringField = "this is a string", arrayField = List("1", "2", "3"), mapField = Map("key" -> 100))
    val json = toJson(test)

    val reconstructed = fromJson[TestSerialization](json)
    reconstructed.stringField should be("this is a string")
    reconstructed.arrayField should be(List("1", "2", "3"))
    reconstructed.mapField should be(Map("key" -> 100))
  }

  test("Should be able to seralise a mongo objectID") {

    val testObjectId = new ObjectId()
    val test = MongoSerialisation(testObjectId)
    val json = toJson(test)

    json should include(testObjectId.toString)
    json should include( """{"id":"""" + testObjectId.toString + """"}""")
  }

  test("Should be able to seralise an option mongo objectID") {

    val testObjectId = new ObjectId()
    val test = MongoSerialisationOption(Some(testObjectId))
    val json = toJson(test)

    json should include(testObjectId.toString)
    json should include( """{"id":"""" + testObjectId.toString + """"}""")
  }

  test("Should be able to deserialise a mongo id") {
    val testObjectId = new ObjectId
    val deserialised = fromJson[MongoSerialisation]( """{"id":"""" + testObjectId.toString + """"}""")
    deserialised.id should be(testObjectId)
  }

  test("Should be able to seralise a date") {
    val testDate = new DateTime(DateTimeZone.UTC)
    val test = DateSerialisation(testDate)
    val json = toJson(test)

    json should include(testDate.toString)
    json should be( """{"date":"""" + testDate.toString() + """"}""")
  }

  test("Should be able to deserialise a date") {
    val testDate = new DateTime(DateTimeZone.UTC)
    val deserialised = fromJson[DateSerialisation]( """{"date":"""" + testDate.toString() + """"}""")
    deserialised.date.toString() should be(testDate.toString())
  }

}