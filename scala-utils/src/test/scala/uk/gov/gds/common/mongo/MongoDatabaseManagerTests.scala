package uk.gov.gds.common.mongo

import migration.{ChangeScriptFailedException, ChangeScript, ChangeScriptStatus}
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import org.scalatest.{GivenWhenThen, FunSuite}
import uk.gov.gds.common.config.Config
import play.api.Logger

class MongoDatabaseManagerTests
  extends FunSuite
  with ShouldMatchers
  with GivenWhenThen
  with MongoDatabaseBackedTest {

  protected def databaseManager = UnauthenticatedMongoDatabaseManagerForTests

  private val simpleChangeScript = SimpleChangeScriptThatDoesNothing()
  private val changeScriptThatThrowsAnException = ChangeScriptThatThrowsAnException()

  test("Changelog audit stores changescript information") {
    given("A database with no changescripts applied but with one script to apply")
    then("the audit table should be empty")

    val databaseManager = DatabaseManager(simpleChangeScript)

    databaseManager.appliedChangeScripts.total should be(0)

    when("We apply the changescript")
    databaseManager.initializeDatabase()

    then("The change log audit should contain the changescript that we applied")
    databaseManager.appliedChangeScripts.total should be(1)

    databaseManager.changeScriptAuditFor(simpleChangeScript.name) match {
      case None =>
        fail("Change script should have been created")
      case Some(changeScriptAuditEntry) =>
        changeScriptAuditEntry.status should be(ChangeScriptStatus.ok)
        val dateOfApplication = changeScriptAuditEntry.runAt

        when("We re-run the change script application")
        databaseManager.initializeDatabase()

        then("The same change script should not be applied twice")
        databaseManager.appliedChangeScripts.total should be(1)

        databaseManager.changeScriptAuditFor(simpleChangeScript.name).get.runAt should be(dateOfApplication)
    }
  }

  test("Fails change script run if a changescript throws an exception") {
    given("A database with no changescripts applied but with two set to apply, with the first one likely to fail")
    val databaseManager = DatabaseManager(changeScriptThatThrowsAnException, simpleChangeScript)

    when("we apply the changescripts")
    evaluating(databaseManager.initializeDatabase()) should produce[ChangeScriptFailedException]

    then("None of the change scripts should have applied due to the exception, and an exception should have been thrown")
    databaseManager.appliedChangeScripts.total should be(1)

    databaseManager.changeScriptAuditFor(changeScriptThatThrowsAnException.name) match {
      case None => fail("Should have found an audit entry for " + changeScriptThatThrowsAnException.name)
      case Some(changeScriptAuditEntry) => changeScriptAuditEntry.status should be(ChangeScriptStatus.failed)
    }
  }

  test("Audit records failed change scripts") {
    given("A database with no changescripts applied, but with one to apply that will fail")
    val databaseManager = DatabaseManager(changeScriptThatThrowsAnException)

    when("we apply the changescripts")
    evaluating(databaseManager.initializeDatabase()) should produce[ChangeScriptFailedException]

    then("The status of the change script in the audit should be recorded as failed")

    databaseManager.changeScriptAuditFor(changeScriptThatThrowsAnException) match {
      case None => fail("Should have found an audit entry for " + changeScriptThatThrowsAnException.name)
      case Some(changeScriptAuditEntry) => changeScriptAuditEntry.status should be(ChangeScriptStatus.failed)
    }
  }

  test("Database connection is established to unauthenticated database") {
    given("No configuration value for username is specified")

    Logger.warn(MongoConfig.authenticated.toString())
    intercept[Exception] {
      Logger.warn(
        Config("mongo.database.auth.username").toString()
      )
    }
    then("the Scala process connects successfully to the unauthenicated db")
    databaseManager.initializeDatabase()

  }

  case class DatabaseManager(changeScripts: ChangeScript*) extends MongoDatabaseManager {
    protected val repositoriesToInitialiseOnStartup = Nil

    override def databaseChangeScripts = changeScripts.toList
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
