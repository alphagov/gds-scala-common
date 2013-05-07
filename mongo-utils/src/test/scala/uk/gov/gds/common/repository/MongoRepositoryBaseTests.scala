package uk.gov.gds.common.repository

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import uk.gov.gds.common.mongo.repository.SyntacticSugarForMongoQueries

class MongoRepositoryBaseTests extends FunSuite
with ShouldMatchers
with MongoDatabaseBackedTest
with SyntacticSugarForMongoQueries {

  protected def databaseManager = SimpleTestDataManagerForTests

  test("Paging through 2 items, pageSize 1 should return 1 at a time") {
    val item1 = SimpleTestData(key = 1, value = "one")
    val item2 = SimpleTestData(key = 2, value = "two")
    SimpleTestDataRepository.safeInsert(item1)
    SimpleTestDataRepository.safeInsert(item2)

    val batch1 = SimpleTestDataRepository.SimpleMongoCursor(query = SimpleTestDataRepository.allFields, pageSize = 1, page = 1).pageOfData
    val batch2 = SimpleTestDataRepository.SimpleMongoCursor(query = SimpleTestDataRepository.allFields, pageSize = 1, page = 2).pageOfData

    batch1.size should be(1)
    batch2.size should be(1)
    batch1(0) should not be(batch2(0))
  }

}
