package uk.gov.gds.common.json

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import JsonSerializer._

case class TestSerialization(stringField: String = "", arrayField: List[String] = Nil, mapField: Map[String, Int] = Map.empty)

class JsonSerializerTests extends FunSuite with ShouldMatchers {

  test("Should be able to turn map to json string") {
    val json = toJson(Map("this" -> "string"))
    json should include("""{"this":"string"}""")
  }

  test("should be able to turn case class into json string") {
    val test = TestSerialization(stringField = "this is a string", arrayField = List("1", "2", "3"), mapField = Map("key" -> 100))
    val json = toJson(test)
    json should include(""""stringField":"this is a string"""")
    json should include(""""arrayField":["1","2","3"]""")
    json should include(""""mapField":{"key":100}""")
  }

  test("Should be able to reconstruct the case class fron json string") {
    val test = TestSerialization(stringField = "this is a string", arrayField = List("1", "2", "3"), mapField = Map("key" -> 100))
    val json = toJson(test)

    val reconstructed = fromJson[TestSerialization](json)
    reconstructed.stringField should be("this is a string")
    reconstructed.arrayField should be(List("1", "2", "3"))
    reconstructed.mapField should be(Map("key" -> 100))
  }
}