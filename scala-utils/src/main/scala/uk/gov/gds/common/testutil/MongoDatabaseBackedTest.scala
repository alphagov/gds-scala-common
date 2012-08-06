package uk.gov.gds.common.testutil

import org.scalatest._
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.mongo.MongoDatabaseManager

trait MongoDatabaseBackedTest extends BeforeAndAfterEach with Logging {
  self: BeforeAndAfterEach with Suite =>

  protected def databaseManager: MongoDatabaseManager

  override protected def beforeEach() {
    IntegrationTestMutex.lock()
    super.beforeEach()
    databaseManager.emptyDatabase()
  }

  override protected def afterEach() {
    try {
      super.afterEach()
    }
    finally {
      IntegrationTestMutex.unlock()
    }
  }
}