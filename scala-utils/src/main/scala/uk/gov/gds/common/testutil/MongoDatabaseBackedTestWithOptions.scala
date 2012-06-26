package uk.gov.gds.common.testutil

import org.scalatest.{BeforeAndAfterEach, Suite, BeforeAndAfterAll}
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.mongo.MongoDatabaseManager


trait MongoDatabaseBackedTestWithOptions extends BeforeAndAfterAll with BeforeAndAfterEach with Logging{

  self: BeforeAndAfterAll with BeforeAndAfterEach with Suite =>

  override protected def beforeAll() {
    IntegrationTestMutex.lock()
    super.beforeAll()
    if (reloadDBBeforeAllTests) {
      logger.info("RELOAD DATABASE (MongoDatabaseBackedTestWithOptions)")
      databaseManager.emptyDatabase()
      databaseManager.initializeDatabase()
    }
  }


  override protected def afterAll() {
    super.afterAll()
    IntegrationTestMutex.unlock()
  }

  protected def databaseManager: MongoDatabaseManager
  protected val reloadDBBeforeAllTests = false
  protected val reloadDBBeforeEachTest = false
}
