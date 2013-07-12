package uk.gov.gds.common.mongo

import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import org.scalatest._

object AuthTest extends Tag("uk.gov.gds.tag.MongoAuthTest")

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

  ignore("Database connection authenticated", AuthTest) {
    Given("Configuration value for username is specified")
    Then("the Scala process connects successfully to the authenticated db")
    // The connection actually happens in mongo database backed test
  }
}
