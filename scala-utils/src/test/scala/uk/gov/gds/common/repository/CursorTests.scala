package uk.gov.gds.common.repository

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.GivenWhenThen
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import uk.gov.gds.common.pagination.PaginationSupport
import uk.gov.gds.common.mongo.repository.SimpleMongoRepository
import uk.gov.gds.common.mongo.MongoDatabaseManagerForTests

class CursorTests
  extends FunSuite
  with ShouldMatchers
  with GivenWhenThen
  with MongoDatabaseBackedTest
  with PaginationSupport {

  test("A cursor with fewer items than the page size returns the correct number of results") {
    given("A collection containing 5 items")
    val items = TestData.createItems(5)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should have a cursor with 5 items in it on the first page")
    cursor.total should be(5)
    cursor.pageOfData.size should be(5)
    cursor.pages should be(1)

    then("each returned items values should match the one created")
    items.zip(cursor.pageOfData).foreach {
      pair =>
        pair._1 should be(pair._2)
    }
  }

  test("A cursor with no results functions correctly") {
    given("a cursor with no items")
    val cursor = TestData.all

    then("the page size and total should both be zero")
    cursor.total should be(0)
    cursor.pageOfData.size should be(0)
    cursor.pages should be(0)
    cursor.hasNextPage should be(false)
    evaluating(cursor.gotoNextPage) should produce[EndOfCursorException]
  }

  test("A cursor with exactly the same amount of results as the page size works correctly") {
    given("a collection with the same amount of results as the page size")

    val items = TestData.createItems(defaultPageSize)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should have a cursor with as single full page of items")
    cursor.total should be(defaultPageSize)
    cursor.pageOfData.size should be(defaultPageSize)
    cursor.pages should be(1)
    cursor.hasNextPage should be(false)

    then("each returned items values should match the one created")
    items.zip(cursor.pageOfData).foreach {
      pair =>
        pair._1 should be(pair._2)
    }
  }

  test("Cursors with sizes greater than the default page size") {
    given("A collection with more items than the default page size")
    TestData.createItems(defaultPageSize + 10)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should have a cursor with less items than the total")
    cursor.total should be(defaultPageSize + 10)
    cursor.pageOfData.size should be(defaultPageSize)
    cursor.pages should be(2)
    cursor.currentPage should be(1)
    cursor.hasNextPage should be(true)

    try {
      cursor.gotoNextPage
      cursor.pageOfData.size should be(10)
      cursor.hasNextPage should be(false)
    }
    catch {
      case e: Exception => fail("Should not have thrown exception", e)
    }
  }

  test("SimpleMongoCursor with twice the page size functions correctly") {
    given("A collection with twice the items as the default page size")
    TestData.createItems(defaultPageSize * 2)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("We should have 2 pages in the result set")
    cursor.total should be(defaultPageSize * 2)
    cursor.pageOfData.size should be(defaultPageSize)
    cursor.pages should be(2)
    cursor.currentPage should be(1)
    cursor.hasNextPage should be(true)

    try {
      cursor.gotoNextPage
      cursor.pageOfData.size should be(defaultPageSize)
      cursor.hasNextPage should be(false)
    }
    catch {
      case e: Exception => fail("Should not have thrown exception", e)
    }
  }

  test("We can select the second page in the result set") {
    given("A collection with twice the items as the default page size")
    val testData = TestData.createItems(defaultPageSize * 2)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to select the second page")
    cursor.gotoNextPage
    cursor.currentPage should be(2)

    then("we should be able to loop through the results on the second page")
    val secondPageOfItemsFromDatabase = testData.drop(defaultPageSize)

    cursor.pageOfData.zip(secondPageOfItemsFromDatabase).foreach {
      pair =>
        pair._1 should be(pair._2)
    }
  }

  test("We can iterate across all results in a single page cursor with a result size that matches the page size") {
    given("A collection with the default page size")
    TestData.createItems(defaultPageSize)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(defaultPageSize)
  }

  test("We can iterate across all results in a single page cursor with fewer results than the default page size") {
    given("A collection fewer items than the default page size")
    TestData.createItems(defaultPageSize - 70)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(defaultPageSize - 70)
  }

  test("We can iterate across all results in a multi page cursor with a non-exact multiple of page size") {
    given("A collection with thrice the items as the default page size")
    TestData.createItems((defaultPageSize * 3) + 50)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(350)
  }

  test("We can iterate across all results in a multi page cursor with an exact multiple of page size") {
    given("A collection with thrice the items as the default page size")
    TestData.createItems(defaultPageSize * 3)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(300)
  }

  test("We can map over all results in a single page cursor with a single page") {
    given("A collection with the same number of items as the default page size")
    val items = TestData.createItems(defaultPageSize)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to map over the total resultset for the cursor")

    val data = cursor.map(item => item)

    items.zip(data).map {
      item =>
        item._1 should be(item._2)
    }
  }

  test("We can map over all results in a multi page cursor") {
    given("A collection with the same number of items as the default page size")
    val items = TestData.createItems(defaultPageSize * 3 + 50)

    when("we load the items from the repository")
    val cursor = TestData.all

    then("we should be able to map over the total resultset for the cursor")

    val data = cursor.map(item => item)

    items.zip(data).map {
      item =>
        item._1 should be(item._2)
    }
  }
}

private[repository] case class Data(key: Int, value: String)

private[repository] object TestData extends SimpleMongoRepository[Data] {
  lazy val collection = MongoDatabaseManagerForTests("test-data")

  def createItems(numberOfItems: Int) = 1.to(numberOfItems).map {
    i =>
      store(Data(key = i, value = "test"))
  }
}
