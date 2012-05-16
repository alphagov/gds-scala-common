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
  private val changeScriptThatThrowsAnException = ChangeScriptThatThrowsAnException()

  test("Changelog audit stores changescript information") {
    given("A database with no changescripts applied but with one script to apply")
    val changeLog = MigrationTestRepostiory(simpleChangeScript)

    then("the audit table should be empty")
    changeLog.all.total should be(0)

    when("We apply the changescript")
    changeLog.applyChangeScripts()

    then("The change log audit should contain the changescript that we applied")
    val appliedChangeScripts = changeLog.all

    appliedChangeScripts.total should be(1)

    val listMatchingOurName = appliedChangeScripts.pageOfData.filter(x => x.name.equals(simpleChangeScript.name))
    listMatchingOurName.size should be(1)
    val changeScriptAuditEntry = listMatchingOurName.head
    val dateOfApplication = changeScriptAuditEntry.runAt

    changeScriptAuditEntry.status should be(ChangeScriptStatus.ok)

    when("We re-run the change script application")
    changeLog.applyChangeScripts()

    then("The same change script should not be applied twice")
    appliedChangeScripts.total should be(1)
    val newListMatchingOurName = appliedChangeScripts.pageOfData.filter(x => x.name.equals(simpleChangeScript.name))
    val newDateOfApplication = newListMatchingOurName.head.runAt

    newListMatchingOurName.size should be(1)
    newDateOfApplication should be(dateOfApplication)
  }

  test("Fails change script run if a changescript throws an exception") {
    given("A database with no changescripts applied but with two set to apply, with the first one likely to fail")
    val changeLog = MigrationTestRepostiory(changeScriptThatThrowsAnException, simpleChangeScript)

    when("we apply the changescripts")
    evaluating(changeLog.applyChangeScripts()) should produce[ChangeScriptFailedException]

    then("None of the change scripts should have applied due to the exception, and an exception should have been thrown")
    changeLog.all.total should be(1)

    changeLog.load(changeScriptThatThrowsAnException) match {
      case None => fail("Should have found an audit entry for " + changeScriptThatThrowsAnException.name)
      case Some(changeScriptAuditEntry) => changeScriptAuditEntry.status should be(ChangeScriptStatus.failed)
    }
  }

  test("Audit records failed change scripts") {
    given("A database with no changescripts applied, but with one to apply that will fail")
    val changeLog = MigrationTestRepostiory(changeScriptThatThrowsAnException)

    when("we apply the changescripts")
    evaluating(changeLog.applyChangeScripts()) should produce[ChangeScriptFailedException]

    then("The status of the change script in the audit should be recorded as failed")

    changeLog.load(changeScriptThatThrowsAnException) match {
      case None => fail("Should have found an audit entry for " + changeScriptThatThrowsAnException.name)
      case Some(changeScriptAuditEntry) => changeScriptAuditEntry.status should be(ChangeScriptStatus.failed)
    }
  }

  case class SimpleChangeScriptThatDoesNothing() extends ChangeScript {
    def applyToDatabase() {}
  }

  case class ChangeScriptThatThrowsAnException() extends ChangeScript {
    def applyToDatabase() {
      throw new Exception()
    }
  }

}
