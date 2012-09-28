package uk.gov.gds.common.mongo

import migration.{ ChangeScriptFailedException, ChangeScript, ChangeScriptStatus }
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.testutil.{ IntegrationTestMutex, MongoDatabaseBackedTest }
import org.scalatest.{ GivenWhenThen, FunSuite }
import play.api.Logger
import xml.dtd.SystemID
import uk.gov.gds.common.config.Config
import com.mongodb.casbah.MongoDB

object AuthenticatedMongoDatabaseManagerForTests extends MongoDatabaseManager {

  // We use the default database manager here, which will authenticate

  protected override lazy val databaseHosts = {
    val databaseHostString = "localhost:27018"
    logger.info("Mongo Database Hosts: " + databaseHostString)
    databaseHostString.split(",").toList
  }

  protected override def databaseUsername = "testuser"

  protected override def databasePasssword = "secret"

  protected override def databaseName = "gdsScalaCommonAuthenticatedTest"
    
  protected val repositoriesToInitialiseOnStartup = Nil
}

class MongoDatabaseManagerAuthenticationTests
  extends FunSuite
  with ShouldMatchers
  with GivenWhenThen
  with MongoDatabaseBackedTest {

  protected def databaseManager = AuthenticatedMongoDatabaseManagerForTests

  test("Database connection authenticated") {
    given("Configuration value for username is specified")
    then("the Scala process connects successfully to the authenticated db")
    // The connection actually happens in mongo database backed test
  }
}
