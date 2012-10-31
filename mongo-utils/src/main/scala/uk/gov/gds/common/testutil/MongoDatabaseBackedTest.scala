package uk.gov.gds.common.testutil

import org.scalatest._
import uk.gov.gds.common.mongo.MongoDatabaseManager
import uk.gov.gds.common.logging.Logging

trait MongoDatabaseBackedTest extends BeforeAndAfterEach with Logging {
  self: BeforeAndAfterEach with Suite =>

  protected def databaseManager: MongoDatabaseManager

  override protected def beforeEach() {
    IntegrationTestMutex.lock()
    logger.warn("Locking for Test " + super.toString)
    super.beforeEach()
    databaseManager.emptyDatabase()
  }

  override protected def afterEach() {
    try {
      super.afterEach()
    }
    finally {
      logger.warn("Unlocking after Test " + super.toString)
      IntegrationTestMutex.unlock()
    }
  }
}