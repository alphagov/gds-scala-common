package uk.gov.gds.common.mongo

import migration.{ChangeScriptFailedException, ChangeScript, ChangeScriptStatus}
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.testutil.{IntegrationTestMutex, MongoDatabaseBackedTest}
import org.scalatest.{GivenWhenThen, FunSuite}
import play.api.Logger
import xml.dtd.SystemID
import uk.gov.gds.common.config.Config

class MongoDatabaseManagerAuthenticationTests
  extends FunSuite
  with ShouldMatchers
  with GivenWhenThen
  with MongoDatabaseBackedTest {

  /* OK so this is not the best, but need to reach into the environment and set the value of gds.mode in order
  to make it so that we can load an alternate config file, also need to cleanup again afterwards.
   */
  override protected def beforeEach() {
    val gdsmode = "testauth"
    System.setProperty("gds.mode", gdsmode)
    Logger.warn("gds_mode=" + gdsmode)
    super.beforeEach()
  }

  override protected def afterEach() {
    System.setProperty("gds.mode", "")
  }

  protected def databaseManager = MongoDatabaseManagerForTests

  test("Database connection authenticated") {
    given("Configuration value for username is specified")
      MongoConfig.authenticated == true
    then("the Scala process connects successfully to the authenticated db")

  }

}
