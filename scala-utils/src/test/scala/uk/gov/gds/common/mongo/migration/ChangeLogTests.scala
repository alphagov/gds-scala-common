package uk.gov.gds.common.mongo.migration

import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import uk.gov.gds.common.mongo.MongoDatabaseManagerForTests
import org.scalatest.{FunSuite, GivenWhenThen}

class ChangeLogTests
  extends FunSuite
  with ShouldMatchers
  with GivenWhenThen
  with MongoDatabaseBackedTest {

  protected def databaseManager = MongoDatabaseManagerForTests

  private val simpleChangeScript = SimpleChangeScriptThatDoesNothing()

  test("Can apply changescript") {
    given("A database with no changescripts applied but with one set to apply")
    val changeLog = MigrationTestRepostiory(simpleChangeScript)

    then("the audit table should be empty")
    changeLog.all.total should be(0)

    when("We apply the changescript")
    changeLog.applyChangeScripts()

    then("The change log audit should contain the changescript that we applied")
    val appliedChangeScripts = changeLog.all

    appliedChangeScripts.total should be(1)

    val listMatchingOurName = appliedChangeScripts.pageOfData.filter(x => x.name.equals(simpleChangeScript.getClass.getName))
    listMatchingOurName.size should be(1)
    val dateOfApplication = listMatchingOurName.head.runAt

    when("We re-run the change script application")
    changeLog.applyChangeScripts()

    then("The same change script should not be applied twice")
    appliedChangeScripts.total should be(1)
    val newListMatchingOurName =  appliedChangeScripts.pageOfData.filter(x => x.name.equals(simpleChangeScript.getClass.getName))
    val newDateOfApplication = newListMatchingOurName.head.runAt

    newListMatchingOurName.size should be(1)
    newDateOfApplication should be(dateOfApplication)
  }

  case class SimpleChangeScriptThatDoesNothing() extends ChangeScript {
    def applyToDatabase() {}
  }

}
