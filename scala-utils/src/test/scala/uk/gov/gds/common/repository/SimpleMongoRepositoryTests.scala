package uk.gov.gds.common.repository

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.mongo.{MongoDatabaseManager}
import uk.gov.gds.common.mongo.repository._
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import com.novus.salat.annotations._
import org.bson.types.ObjectId
import com.mongodb.MongoException
import com.mongodb.casbah.Imports._
import java.io.ByteArrayOutputStream
import uk.gov.gds.common.mongo.UnauthenticatedMongoDatabaseManagerForTests

class SimpleMongoRepositoryTests extends FunSuite
with ShouldMatchers
with MongoDatabaseBackedTest
with SyntacticSugarForMongoQueries {

  protected def databaseManager = SimpleTestDataManagerForTests

  test("Should create amongo id on inserting a new row") {
    SimpleTestDataRepository.safeInsert(SimpleTestData(key = 1, value = "test-2")).id should not be(None)
  }

  test("Should be able to query for data retrieveing an object of the correct type") {
    SimpleTestDataRepository.createItems(1)
    val results = SimpleTestDataRepository.findOne(where("value" -> "test"))
    results.get.value should be("test")
  }

  test("Should be able to save objects in a safe manner - that is exceptions are thrown and no objects are persisted on error") {
    val caught = intercept[MongoException] {
      SimpleTestDataRepository.safeInsert(SimpleTestData(key = 1, value = "test-2"))
      SimpleTestDataRepository.safeInsert(SimpleTestData(key = 2, value = "test-2"))
    }

    caught.getMessage should include("E11000 duplicate key error index: gdsScalaCommonTest.testFindData.$value_1")
  }

  test("Should be able to save an object in a fire and forget manner - not waiting for errors") {
    SimpleTestDataRepository.unsafeInsert(SimpleTestData(key = 1, value = "this-3"))
    SimpleTestDataRepository.unsafeInsert(SimpleTestData(key = 2, value = "this-3"))

    // Need to wait a sec as a fire and forget insert is fast
    Thread.sleep(1000)

    SimpleTestDataRepository.findOne(where("key" -> 1)).get.value should be("this-3")
    SimpleTestDataRepository.findOne(where("key" -> 2)) should be(None)
  }

  test("Should be able to update an existing object") {
    SimpleTestDataRepository.safeInsert(SimpleTestData(key = 1, value = "update-test"))
    val Some(results) = SimpleTestDataRepository.findOne(where("value" -> "update-test"))
    results.value should be("update-test")
    SimpleTestDataRepository.safeUpdate(where("value" -> "update-test"), $set("value" -> "new-update-test"))
    val Some(updatedResults) = SimpleTestDataRepository.findOne(where("value" -> "new-update-test"))
    updatedResults.value should be("new-update-test")
  }

  test("Should throw an exception on safe update when an error happens") {
    SimpleTestDataRepository.safeInsert(SimpleTestData(key = 1, value = "update-test-1"))
    SimpleTestDataRepository.safeInsert(SimpleTestData(key = 2, value = "update-test-2"))

    val caught = intercept[MongoException]{
      SimpleTestDataRepository.safeUpdate(where("key" -> 2), $set("value" -> "update-test-1"))
    }
    caught.getMessage should include("E11000 duplicate key error index: gdsScalaCommonTest.testFindData.$value_1")
  }

  
   test("Should be able to dump JSON to output stream") {
    val id1 = SimpleTestDataRepository.safeInsert(SimpleTestData(key = 1, value = "update-test-1")).id.get
    val id2 = SimpleTestDataRepository.safeInsert(SimpleTestData(key = 2, value = "update-test-2")).id.get
    val os = new ByteArrayOutputStream
    SimpleTestDataRepository.dumpJSON(os)
    val expected = """|{"_id":{"$oid":"id1"},"key":1,"value":"update-test-1"}
    				  |{"_id":{"$oid":"id2"},"key":2,"value":"update-test-2"}
                      |""".stripMargin.replace("id1", id1.toString()).replace("id2", id2.toString())
    val actual = new String(os.toByteArray())                       
    actual should equal (expected)
  }

  test("Should be able to get an object not wrapped in an option when it exists in the database") {
    val id = SimpleTestDataRepository.safeInsert(SimpleTestData(key = 1, value = "get-test")).id.get

    val obj = SimpleTestDataRepository.get(id)
    obj.key should be(1)
    obj.value should be("get-test")
  }

  test("Calling get for an id that does not exist should throw an exception") {
    val id = new ObjectId()

    val caught = intercept[NoSuchObjectException] {
      SimpleTestDataRepository.get(id)
    }

    caught.id should be(id.toString)
  }
}

object SimpleTestDataManagerForTests extends MongoDatabaseManager {
  protected val repositoriesToInitialiseOnStartup = List(SimpleTestDataRepository)
}

case class SimpleTestData(@Key("_id") id: Option[ObjectId] = None, key: Int, value: String)

object SimpleTestDataRepository extends SimpleMongoRepository[SimpleTestData] {

  lazy val collection = UnauthenticatedMongoDatabaseManagerForTests("testFindData")

  override protected def createIndexes() {
    addIndex(index("value" -> Ascending), unique = Enforced, sparse = Complete)
    addIndex(index("key" -> Ascending), unique = Enforced, sparse = Complete)
    super.createIndexes()
  }

  def createItems(numberOfItems: Int) = 1.to(numberOfItems).map {
    i =>
      safeInsert(SimpleTestData(key = i, value = "test"))
  }
}

