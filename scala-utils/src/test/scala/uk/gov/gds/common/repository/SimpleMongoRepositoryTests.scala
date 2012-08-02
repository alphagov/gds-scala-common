package uk.gov.gds.common.repository

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.mongo.{MongoDatabaseManager, MongoDatabaseManagerForTests}
import uk.gov.gds.common.mongo.repository._
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import com.novus.salat.annotations._
import org.bson.types.ObjectId
import com.mongodb.MongoException

class SimpleMongoRepositoryTests extends FunSuite
with ShouldMatchers
with MongoDatabaseBackedTest
with SyntacticSugarForMongoQueries {

  protected def databaseManager = SimpleTestDataManagerForTests

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

    caught.getMessage should include("E11000 duplicate key error index: gds-scala-common-test.testFindData.$value_1")
  }

  test("Should be able to save an object in a fire and forget manner - not waiting for errors") {
    SimpleTestDataRepository.unsafeInsert(SimpleTestData(key = 1, value = "this-3"))
    SimpleTestDataRepository.unsafeInsert(SimpleTestData(key = 2, value = "this-3"))

    // Need to wait a sec as a fire and forget insert is fast
    Thread.sleep(1000)

    SimpleTestDataRepository.findOne(where("key" -> 1)).get.value should be("this-3")
    SimpleTestDataRepository.findOne(where("key" -> 2)) should be(None)
  }
}

object SimpleTestDataManagerForTests extends MongoDatabaseManager {
  protected val repositoriesToInitialiseOnStartup = List(SimpleTestDataRepository)
}

case class SimpleTestData(@Key("_id") id: Option[ObjectId] = None, key: Int, value: String)

object SimpleTestDataRepository extends SimpleMongoRepository[SimpleTestData] {

  lazy val collection = MongoDatabaseManagerForTests("testFindData")

  override protected def createIndexes() {
    addIndex(index("value" -> Ascending), unique = Enforced, sparse = Complete)
    super.createIndexes()
  }

  def createItems(numberOfItems: Int) = 1.to(numberOfItems).map {
    i =>
      safeInsert(SimpleTestData(key = i, value = "test"))
  }
}

