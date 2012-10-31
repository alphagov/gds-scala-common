package uk.gov.gds.common.repository

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.GivenWhenThen
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import uk.gov.gds.common.pagination.PaginationSupport
import uk.gov.gds.common.mongo.{MongoDatabaseManager, UnauthenticatedMongoDatabaseManagerForTests}
import org.joda.time.DateTime
import uk.gov.gds.common.mongo.repository._

class CursorTests
  extends FunSuite
  with ShouldMatchers
  with GivenWhenThen
  with MongoDatabaseBackedTest
  with PaginationSupport
  with SyntacticSugarForMongoQueries{

  protected def databaseManager = SimpleTestDataManagerForCursorTests

  test("A cursor can be converted to a list") {
    given("A collection containing 5 items")
    val items = TestDataRepository.createItems(5)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should have a cursor with 5 items in it on the first page")
    cursor.total should be(5)
    cursor.pageOfData.size should be(5)
    cursor.pages should be(1)

    then("we should be able to convert that cursor to a list, serializing all of the information from the database")
    val list = cursor.toList

    list.size should be(5)
    list should be(items)
  }

  test("A cursor with fewer items than the page size returns the correct number of results") {
    given("A collection containing 5 items")
    val items = TestDataRepository.createItems(5)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

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
    val cursor = TestDataRepository.sorted

    then("the page size and total should both be zero")
    cursor.total should be(0)
    cursor.pageOfData.size should be(0)
    cursor.pages should be(0)
    cursor.hasNextPage should be(false)
    evaluating(cursor.gotoNextPage) should produce[EndOfCursorException]
  }

  test("A cursor with exactly the same amount of results as the page size works correctly") {
    given("a collection with the same amount of results as the page size")

    val items = TestDataRepository.createItems(defaultPageSize)

    Thread.sleep(1000)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    println(cursor.pageOfData)

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
    TestDataRepository.createItems(defaultPageSize + 10)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

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
    TestDataRepository.createItems(defaultPageSize * 2)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

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
    val testData = TestDataRepository.createItems(defaultPageSize * 2)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

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
    TestDataRepository.createItems(defaultPageSize)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(defaultPageSize)
  }

  test("We can iterate across all results in a single page cursor with fewer results than the default page size") {
    given("A collection fewer items than the default page size")
    TestDataRepository.createItems(defaultPageSize - 70)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(defaultPageSize - 70)
  }

  test("We can iterate across all results in a multi page cursor with a non-exact multiple of page size") {
    given("A collection with thrice the items as the default page size")
    TestDataRepository.createItems((defaultPageSize * 3) + 50)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(350)
  }

  test("We can iterate across all results in a multi page cursor with an exact multiple of page size") {
    given("A collection with thrice the items as the default page size")
    TestDataRepository.createItems(defaultPageSize * 3)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should be able to foreach over the total resultset for the cursor")

    var total = 0
    cursor.foreach(item => total = item.key)
    total should be(300)
  }

  test("We can map over all results in a single page cursor with a single page") {
    given("A collection with the same number of items as the default page size")
    val items = TestDataRepository.createItems(defaultPageSize)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should be able to map over the total resultset for the cursor")

    val data = cursor.map(item => item)

    items.zip(data).map {
      item =>
        item._1 should be(item._2)
    }
  }

  test("We can map over all results in a multi page cursor") {
    given("A collection with the same number of items as the default page size")
    val items = TestDataRepository.createItems(defaultPageSize * 3 + 50)

    when("we load the items from the repository")
    val cursor = TestDataRepository.sorted

    then("we should be able to map over the total resultset for the cursor")

    val data = cursor.map(item => item)

    items.zip(data).map {
      item =>
        item._1 should be(item._2)
    }
  }

  test("we can retreive a cursor of data with a date based query") {
    given("some test data that has a date component")
    TestDateDataRepository.createItems(5)

    when("we load them back using a date based query")
    val cursor = TestDateDataRepository.load(filter = where("name" -> in(TestDateDataRepository.keys)), timeQuery = TestDateDataRepository.lt(new DateTime))
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct order")
    cursor.total should be(5)
    data(0).name should be("1")
    data(1).name should be("2")
    data(2).name should be("3")
    data(3).name should be("4")
    data(4).name should be("5")
  }

  test("we can retreive a cursor of data with a date based query - with order reveresed") {
    
    given("some test data that has a date component")
    TestDateDataRepository.createItems(5)

    when("we load them back using a date based query")
    val cursor = TestDateDataRepository.load(filter = where("name" -> in(TestDateDataRepository.keys)), timeQuery = TestDateDataRepository.lt(new DateTime), sort = Ascending)
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct (revered) order")
    cursor.total should be(5)
    data(0).name should be("5")
    data(1).name should be("4")
    data(2).name should be("3")
    data(3).name should be("2")
    data(4).name should be("1")
  }

  test("we can retreive a cursor of data with a date based query - with query direction of greater than given date") {

    given("some test data that has a date component")
    TestDateDataRepository.createItems(5)

    when("we load them back using a date based query")
    val cursor = TestDateDataRepository.load(filter = where("name" -> in(TestDateDataRepository.keys)), timeQuery = TestDateDataRepository.gt((new DateTime).minusHours(36)))
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct (revered) order")
    cursor.total should be(1)
    data(0).name should be("1")
  }

  test("we can retreive a cursor of data with a date based query - with query direction of less than given date") {

    given("some test data that has a date component")
    TestDateDataRepository.createItems(5)

    when("we load them back using a date based query")
    val cursor = TestDateDataRepository.load(filter = where("name" -> in(TestDateDataRepository.keys)), timeQuery = TestDateDataRepository.lt((new DateTime).minusHours(36)))
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct (revered) order")
    cursor.total should be(4)
    data(0).name should be("2")
    data(1).name should be("3")
    data(2).name should be("4")
    data(3).name should be("5")
  }

  test("we can retreive a cursor of data with a date based query filtered by a field") {

    given("some test data that has a date component")
    TestDateDataRepository.createItems(5)

    when("we load them back using a date based query")
    val cursor = TestDateDataRepository.load(where("name" -> "5"), TestDateDataRepository.lt(new DateTime))
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct (revered) order")
    cursor.total should be(1)
    data(0).name should be("5")
  }

  test("we can retreive a cursor of data with a date based query - with page size altered") {

    given("some test data that has a date component")
    TestDateDataRepository.createItems(5)

    when("we load them back using a date based query")
    val cursor = TestDateDataRepository.load(filter = where("name" -> in(TestDateDataRepository.keys)), timeQuery = TestDateDataRepository.lt((new DateTime).minusHours(36)), pageSize = 1)
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct (revered) order")
    cursor.total should be(4)
    cursor.pageOfData.size should be(1)
    data(0).name should be("2")
  }

  test("we can retreive a cursor of data with a date based query - with 2 colums filtered") {

    given("some test data that has a date component")
    Test2ColumnDateDataRepository.createItemsWithTwoFilterColumns(5)

    when("we load them back using a date based query")
    val cursor = Test2ColumnDateDataRepository.load(filter = where("name" -> "1", "otherName" -> "11"), timeQuery = TestDateDataRepository.lt(new DateTime), pageSize = 1)
    val data = cursor.map(item => item)

    then("we should have a data set of the correct size and content in the correct) order")
    cursor.total should be(1)
    cursor.pageOfData.size should be(1)
    data(0).name should be("1")
    data(0).otherName should be("11")
  }

}

object SimpleTestDataManagerForCursorTests extends MongoDatabaseManager {
  protected val repositoriesToInitialiseOnStartup = List(TestDataRepository,TestDateDataRepository, Test2ColumnDateDataRepository)
}

private[repository] case class Data(key: Int, value: String)

private[repository] object TestDataRepository extends SimpleMongoRepository[Data] {

  lazy val collection = UnauthenticatedMongoDatabaseManagerForTests("testData")

  def sorted = SimpleMongoCursor(emptyQuery, order("key" -> Ascending.order))

  def createItems(numberOfItems: Int) = 1.to(numberOfItems).map {
    i =>
      safeInsert(Data(key = i, value = "test"))
  }
}

private[repository] case class DataWithTimestampField(name: String, dateOfBirth: DateTime) extends HasTimestamp

private[repository] case class DataWithTimestampFieldAndSecondColumn(name: String, otherName: String, dateOfBirth: DateTime) extends HasTimestamp

private[repository] object TestDateDataRepository extends TimestampBasedMongoRepository[DataWithTimestampField] {
  lazy val collection = UnauthenticatedMongoDatabaseManagerForTests("testDateData")
  lazy val now = new DateTime
  val databaseTimeStampProperty = "dateOfBirth"

  lazy val keys = List("1","2","3","4","5")

  def createItems(numberOfItems: Int) = 1.to(numberOfItems).map {
    i => safeInsert(DataWithTimestampField(name = i.toString, dateOfBirth = now.minusDays(i)))
  }

}

private[repository] object Test2ColumnDateDataRepository extends TimestampBasedMongoRepository[DataWithTimestampFieldAndSecondColumn] {
  lazy val collection = UnauthenticatedMongoDatabaseManagerForTests("testDateDataTwoColumn")
  lazy val now = new DateTime
  val databaseTimeStampProperty = "dateOfBirth"

  lazy val keys = List("1","2","3","4","5")

  def createItemsWithTwoFilterColumns(numberOfItems: Int) = 1.to(numberOfItems).map {
    i => safeInsert(DataWithTimestampFieldAndSecondColumn(name = i.toString, otherName = (i +10).toString, dateOfBirth = now.minusDays(i)))
  }

}
