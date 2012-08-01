package uk.gov.gds.common.repository

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.GivenWhenThen
import uk.gov.gds.common.pagination.PaginationSupport
import uk.gov.gds.common.mongo.MongoDatabaseManagerForTests
import uk.gov.gds.common.mongo.repository._
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest

class SimpleRepositoryTests extends FunSuite
with ShouldMatchers
with GivenWhenThen
with MongoDatabaseBackedTest
with PaginationSupport
with SyntacticSugarForMongoQueries {

  protected def databaseManager = MongoDatabaseManagerForTests

  test("Should be able to query for data") {
    FindTestData.createItems(1)
    val results = FindTestData.findOne(where("value" -> "test"))
    results.get.value should be("test")
  }

}

private case class FindData(key: Int, value: String)

private object FindTestData extends SimpleMongoRepository[FindData] {

  lazy val collection = MongoDatabaseManagerForTests("testFindData")

  def createItems(numberOfItems: Int) = 1.to(numberOfItems).map {
    i =>
      store(FindData(key = i, value = "test"))
  }
}

